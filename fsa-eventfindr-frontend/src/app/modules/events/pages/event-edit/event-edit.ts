import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, OnDestroy, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { concat, Observable } from 'rxjs';
import { EventApi } from '../../event-api';
import { EventMediaApi } from '../../event-media-api';
import { Event, EventMedia, EventArtistRequest } from '../../event.model';
import { GENRES } from '../../genre.constants';
import { ToastService } from '../../../../core/services/toast';
import { User } from '../../../../core/auth/auth.model';
import { UserApi } from '../../../profile/user-api';
import { toApiError } from '../../../../core/http/api-error';

interface FilePreview {
  file: File;
  previewUrl: string;
  type: 'IMAGE' | 'VIDEO';
}

interface SelectedArtist {
  artistUserId?: number;
  artistName: string;
  registered: boolean;
}

@Component({
  selector: 'app-event-edit',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './event-edit.html',
  styleUrl: './event-edit.scss'
})
export class EventEditComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly eventApi = inject(EventApi);
  private readonly eventMediaApi = inject(EventMediaApi);
  private readonly userApi = inject(UserApi);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly errorMessage = signal('');
  readonly event = signal<Event | null>(null);
  readonly existingMedia = signal<EventMedia[]>([]);
  readonly selectedFiles = signal<FilePreview[]>([]);
  readonly selectedArtists = signal<SelectedArtist[]>([]);
  readonly artistSearchResults = signal<User[]>([]);
  readonly artistSearchOpen = signal(false);
  readonly artistSearchQuery = signal('');
  readonly customArtistName = signal('');
  readonly uploadTotal = signal(0);
  readonly uploadCompleted = signal(0);
  readonly uploadDone = signal(false);
  readonly currentFilePercent = signal(0);
  readonly currentFileName = signal('');

  private readonly cdr = inject(ChangeDetectorRef);
  private searchTimer: ReturnType<typeof setTimeout> | null = null;
  private blurTimer: ReturnType<typeof setTimeout> | null = null;

  readonly MAX_IMAGES = 9;
  readonly MAX_VIDEO = 1;
  readonly MAX_IMAGE_SIZE = 5 * 1024 * 1024;
  readonly MAX_VIDEO_SIZE = 75 * 1024 * 1024;
  readonly ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
  readonly ALLOWED_VIDEO_TYPES = ['video/mp4', 'video/quicktime'];

  readonly GENRES = GENRES;
  readonly genreSearchResults = signal<string[]>([]);
  readonly genreSearchOpen = signal(false);
  private genreBlurTimer: ReturnType<typeof setTimeout> | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    location: ['', [Validators.required, Validators.maxLength(300)]],
    eventDate: ['', Validators.required],
    description: ['', Validators.maxLength(2000)],
    genre: [''],
    price: [null as number | null, [Validators.min(0)]],
    ticketUrl: ['', Validators.maxLength(500)],
    capacity: [null as number | null, [Validators.min(1)]]
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/events/my']);
      return;
    }

    this.eventApi.getEventById(id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (event) => {
        this.event.set(event);
        const date = new Date(event.eventDate);
        const localDate = date.getFullYear() + '-' +
          String(date.getMonth() + 1).padStart(2, '0') + '-' +
          String(date.getDate()).padStart(2, '0') + 'T' +
          String(date.getHours()).padStart(2, '0') + ':' +
          String(date.getMinutes()).padStart(2, '0');

        this.form.patchValue({
          name: event.name,
          location: event.location,
          eventDate: localDate,
          description: event.description ?? '',
          genre: event.genre ?? '',
          price: event.price ?? null,
          ticketUrl: event.ticketUrl ?? '',
          capacity: event.capacity ?? null
        });

        if (event.artists) {
          this.selectedArtists.set(event.artists.map(a => ({
            artistUserId: a.artistUserId,
            artistName: a.artistName,
            registered: !!a.artistUserId
          })));
        }

        this.eventMediaApi.getEventMedia(id).pipe(
          takeUntilDestroyed(this.destroyRef)
        ).subscribe({
          next: (media) => this.existingMedia.set(media),
          error: () => {}
        });

        this.loading.set(false);
      },
      error: () => {
        this.toast.error('Failed to load event.');
        this.router.navigate(['/events/my']);
      }
    });
  }

  mediaUrl(media: EventMedia): string {
    return this.eventMediaApi.resolveUrl(media.url);
  }

  onDeleteMedia(media: EventMedia): void {
    const ev = this.event();
    if (!ev) return;
    this.eventMediaApi.deleteMedia(ev.id, media.id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.existingMedia.update(list => list.filter(m => m.id !== media.id));
        this.toast.success('Media deleted.');
      },
      error: () => this.toast.error('Failed to delete media.')
    });
  }

  onMoveMedia(media: EventMedia, direction: -1 | 1): void {
    const ev = this.event();
    if (!ev) return;
    const list = [...this.existingMedia()];
    const idx = list.findIndex(m => m.id === media.id);
    const newIdx = idx + direction;
    if (newIdx < 0 || newIdx >= list.length) return;
    [list[idx], list[newIdx]] = [list[newIdx], list[idx]];
    this.existingMedia.set(list);
    this.eventMediaApi.reorderMedia(ev.id, list.map(m => m.id)).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      error: () => this.toast.error('Failed to reorder media.')
    });
  }

  onGenreInput(): void {
    const value = this.form.controls.genre.value.toLowerCase().trim();
    if (!value) {
      this.genreSearchResults.set([...GENRES]);
      this.genreSearchOpen.set(true);
      return;
    }
    const filtered = GENRES.filter(g => g.toLowerCase().includes(value));
    this.genreSearchResults.set(filtered);
    this.genreSearchOpen.set(filtered.length > 0);
  }

  onGenreFocus(): void {
    if (this.genreBlurTimer) { clearTimeout(this.genreBlurTimer); this.genreBlurTimer = null; }
    const value = this.form.controls.genre.value.toLowerCase().trim();
    const filtered = value ? GENRES.filter(g => g.toLowerCase().includes(value)) : [...GENRES];
    this.genreSearchResults.set(filtered);
    this.genreSearchOpen.set(filtered.length > 0);
  }

  selectGenre(genre: string): void {
    if (this.genreBlurTimer) { clearTimeout(this.genreBlurTimer); this.genreBlurTimer = null; }
    this.form.controls.genre.setValue(genre);
    this.genreSearchOpen.set(false);
  }

  closeGenreDropdown(): void {
    if (this.genreBlurTimer) clearTimeout(this.genreBlurTimer);
    this.genreBlurTimer = setTimeout(() => {
      this.genreSearchOpen.set(false);
      this.cdr.markForCheck();
    }, 200);
  }

  onArtistSearchInput(event: globalThis.Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.artistSearchQuery.set(value);
    if (this.searchTimer) clearTimeout(this.searchTimer);
    if (value.length < 2) {
      this.artistSearchResults.set([]);
      this.artistSearchOpen.set(false);
      return;
    }
    this.searchTimer = setTimeout(() => this.doArtistSearch(value), 300);
  }

  onArtistSearchKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      const q = this.artistSearchQuery().trim();
      if (q.length >= 2) {
        if (this.searchTimer) clearTimeout(this.searchTimer);
        this.doArtistSearch(q);
      }
    }
  }

  doArtistSearch(query: string): void {
    if (query.length < 2) return;
    if (this.blurTimer) { clearTimeout(this.blurTimer); this.blurTimer = null; }
    this.userApi.searchArtists(query).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (results) => {
        if (this.blurTimer) { clearTimeout(this.blurTimer); this.blurTimer = null; }
        const selected = this.selectedArtists();
        const filtered = results.filter(r => !selected.some(s => s.artistUserId === r.id));
        this.artistSearchResults.set(filtered);
        this.artistSearchOpen.set(filtered.length > 0);
      },
      error: () => {
        this.artistSearchResults.set([]);
        this.artistSearchOpen.set(false);
      }
    });
  }

  selectArtist(user: User): void {
    if (this.blurTimer) { clearTimeout(this.blurTimer); this.blurTimer = null; }
    const current = this.selectedArtists();
    if (current.some(a => a.artistUserId === user.id)) return;
    this.selectedArtists.set([...current, {
      artistUserId: user.id,
      artistName: user.artistName ?? user.name,
      registered: true
    }]);
    this.artistSearchOpen.set(false);
    this.artistSearchResults.set([]);
    this.artistSearchQuery.set('');
  }

  addCustomArtist(): void {
    const name = this.customArtistName().trim();
    if (!name) return;
    const current = this.selectedArtists();
    if (current.some(a => a.artistName.toLowerCase() === name.toLowerCase())) return;
    this.selectedArtists.set([...current, { artistName: name, registered: false }]);
    this.customArtistName.set('');
  }

  removeArtist(index: number): void {
    const artists = [...this.selectedArtists()];
    artists.splice(index, 1);
    this.selectedArtists.set(artists);
  }

  closeArtistDropdown(): void {
    if (this.blurTimer) clearTimeout(this.blurTimer);
    this.blurTimer = setTimeout(() => this.artistSearchOpen.set(false), 200);
  }

  onCustomArtistKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.addCustomArtist();
    }
  }

  onCustomArtistInput(event: globalThis.Event): void {
    this.customArtistName.set((event.target as HTMLInputElement).value);
  }

  get imageCount(): number {
    return this.existingMedia().filter(m => m.mediaType === 'IMAGE').length +
      this.selectedFiles().filter(f => f.type === 'IMAGE').length;
  }

  get videoCount(): number {
    return this.existingMedia().filter(m => m.mediaType === 'VIDEO').length +
      this.selectedFiles().filter(f => f.type === 'VIDEO').length;
  }

  onFilesSelected(event: globalThis.Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;
    const current = this.selectedFiles();
    const newFiles: FilePreview[] = [];

    for (let i = 0; i < input.files.length; i++) {
      const file = input.files[i];
      const isImage = this.ALLOWED_IMAGE_TYPES.includes(file.type);
      const isVideo = this.ALLOWED_VIDEO_TYPES.includes(file.type);
      if (!isImage && !isVideo) { this.errorMessage.set(`Unsupported file type: ${file.type}`); continue; }
      if (isImage && file.size > this.MAX_IMAGE_SIZE) { this.errorMessage.set(`Image "${file.name}" exceeds 5 MB limit.`); continue; }
      if (isVideo && file.size > this.MAX_VIDEO_SIZE) { this.errorMessage.set(`Video "${file.name}" exceeds 75 MB limit.`); continue; }

      if (isImage && this.imageCount + newFiles.filter(f => f.type === 'IMAGE').length >= this.MAX_IMAGES) {
        this.errorMessage.set(`Maximum ${this.MAX_IMAGES} images allowed.`); continue;
      }
      if (isVideo && this.videoCount + newFiles.filter(f => f.type === 'VIDEO').length >= this.MAX_VIDEO) {
        this.errorMessage.set(`Maximum ${this.MAX_VIDEO} video allowed.`); continue;
      }

      newFiles.push({ file, previewUrl: URL.createObjectURL(file), type: isImage ? 'IMAGE' : 'VIDEO' });
    }

    if (newFiles.length > 0) this.selectedFiles.set([...current, ...newFiles]);
    input.value = '';
  }

  removeFile(index: number): void {
    const files = [...this.selectedFiles()];
    const removed = files.splice(index, 1);
    removed.forEach(f => URL.revokeObjectURL(f.previewUrl));
    this.selectedFiles.set(files);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const ev = this.event();
    if (!ev) return;

    this.submitting.set(true);
    this.errorMessage.set('');

    const value = this.form.getRawValue();
    const artists: EventArtistRequest[] = this.selectedArtists().map(a => ({
      artistUserId: a.artistUserId,
      artistName: a.artistName
    }));
    const request = {
      name: value.name,
      location: value.location,
      eventDate: new Date(value.eventDate).toISOString(),
      description: value.description || undefined,
      genre: value.genre || undefined,
      status: ev.status ?? 'PUBLISHED' as const,
      price: value.price ?? undefined,
      ticketUrl: value.ticketUrl || undefined,
      capacity: value.capacity ?? undefined,
      artists: artists.length > 0 ? artists : undefined
    };

    this.eventApi.updateEvent(ev.id, request).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        const files = this.selectedFiles();
        if (files.length === 0) {
          this.toast.success('Event updated!');
          this.router.navigate(['/events', ev.id]);
          return;
        }

        this.uploadTotal.set(files.length);
        this.uploadCompleted.set(0);
        this.currentFilePercent.set(0);
        this.currentFileName.set(files[0].file.name);

        const uploads = files.map(f =>
          new Observable<void>(sub => {
            this.currentFileName.set(f.file.name);
            this.currentFilePercent.set(0);
            this.eventMediaApi.uploadMedia(ev.id, f.file, pct => this.currentFilePercent.set(pct)).subscribe({
              next: () => { this.uploadCompleted.update(c => c + 1); this.currentFilePercent.set(100); sub.next(); sub.complete(); },
              error: err => sub.error(err)
            });
          })
        );

        concat(...uploads).pipe(
          takeUntilDestroyed(this.destroyRef)
        ).subscribe({
          error: () => { this.submitting.set(false); this.toast.warning('Event updated but some files failed to upload.'); },
          complete: () => {
            this.uploadDone.set(true);
            this.toast.success('Event updated!');
            setTimeout(() => this.router.navigate(['/events', ev.id]), 1500);
          }
        });
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        this.toast.error(toApiError(error).message);
      }
    });
  }

  onPublish(): void {
    const ev = this.event();
    if (!ev) return;
    this.eventApi.publishEvent(ev.id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.event.set({ ...ev, status: 'PUBLISHED' });
        this.toast.success('Event published!');
      },
      error: () => this.toast.error('Failed to publish event.')
    });
  }

  isInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.invalid && control.touched;
  }

  ngOnDestroy(): void {
    if (this.searchTimer) clearTimeout(this.searchTimer);
    if (this.blurTimer) clearTimeout(this.blurTimer);
    if (this.genreBlurTimer) clearTimeout(this.genreBlurTimer);
    for (const file of this.selectedFiles()) {
      URL.revokeObjectURL(file.previewUrl);
    }
  }
}
