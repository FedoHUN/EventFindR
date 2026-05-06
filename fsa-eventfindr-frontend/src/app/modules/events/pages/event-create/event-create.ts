import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EventApi } from '../../event-api';

@Component({
  selector: 'app-event-create',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './event-create.html',
  styleUrl: './event-create.scss'
})
export class EventCreateComponent {
  private readonly fb = inject(FormBuilder);
  private readonly eventApi = inject(EventApi);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly errorMessage = signal('');

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    location: ['', [Validators.required, Validators.maxLength(300)]],
    eventDate: ['', Validators.required],
    description: ['', Validators.maxLength(2000)],
    performers: ['', Validators.maxLength(500)],
    price: [null as number | null, [Validators.min(0)]],
    ticketUrl: ['', Validators.maxLength(500)],
    imageUrl: ['', Validators.maxLength(500)]
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set('');

    const value = this.form.getRawValue();
    const request = {
      name: value.name,
      location: value.location,
      eventDate: new Date(value.eventDate).toISOString(),
      description: value.description || undefined,
      performers: value.performers || undefined,
      price: value.price ?? undefined,
      ticketUrl: value.ticketUrl || undefined,
      imageUrl: value.imageUrl || undefined
    };

    this.eventApi.createEvent(request).subscribe({
      next: () => {
        this.router.navigate(['/events']);
      },
      error: (err) => {
        this.submitting.set(false);
        if (err.status === 403) {
          this.errorMessage.set('You do not have permission to create events.');
        } else if (err.status === 401) {
          this.errorMessage.set('Please log in to create events.');
        } else {
          this.errorMessage.set('Something went wrong. Please try again.');
        }
      }
    });
  }

  isInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.invalid && control.touched;
  }
}
