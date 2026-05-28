import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { DestroyRef, Injectable, PLATFORM_ID, computed, effect, inject, signal } from '@angular/core';

export type ThemeChoice = 'light' | 'dark' | 'system';
export type ResolvedTheme = 'light' | 'dark';

const STORAGE_KEY = 'ef-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly systemPrefersDark = signal(false);

  readonly theme = signal<ThemeChoice>(this.readStoredChoice());

  readonly resolvedTheme = computed<ResolvedTheme>(() => {
    const choice = this.theme();
    if (choice === 'system') {
      return this.systemPrefersDark() ? 'dark' : 'light';
    }
    return choice;
  });

  readonly isDark = computed(() => this.resolvedTheme() === 'dark');

  constructor() {
    if (this.isBrowser && typeof window.matchMedia === 'function') {
      const mq = window.matchMedia('(prefers-color-scheme: dark)');
      const onPreferenceChange = (event: MediaQueryListEvent) => this.systemPrefersDark.set(event.matches);
      this.systemPrefersDark.set(mq.matches);
      mq.addEventListener('change', onPreferenceChange);
      this.destroyRef.onDestroy(() => mq.removeEventListener('change', onPreferenceChange));
    }

    effect(() => {
      const resolved = this.resolvedTheme();
      this.document.documentElement.setAttribute('data-bs-theme', resolved);
    });
  }

  setTheme(choice: ThemeChoice): void {
    this.theme.set(choice);
    if (this.isBrowser) {
      localStorage.setItem(STORAGE_KEY, choice);
    }
  }

  cycleTheme(): void {
    const order: ThemeChoice[] = ['light', 'dark', 'system'];
    const idx = order.indexOf(this.theme());
    this.setTheme(order[(idx + 1) % order.length]);
  }

  private readStoredChoice(): ThemeChoice {
    if (!this.isBrowser) return 'system';
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'light' || stored === 'dark' || stored === 'system') return stored;
    return 'system';
  }
}
