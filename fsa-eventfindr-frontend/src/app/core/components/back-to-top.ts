import { ChangeDetectionStrategy, Component, signal, OnInit, OnDestroy, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-back-to-top',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (visible()) {
      <button
        class="back-to-top-btn"
        (click)="scrollToTop()"
        aria-label="Back to top">
        <i class="bi bi-arrow-up"></i>
      </button>
    }
  `,
  styles: `
    .back-to-top-btn {
      position: fixed;
      bottom: 2rem;
      right: 2rem;
      z-index: 1050;
      width: 44px;
      height: 44px;
      border-radius: 50%;
      border: none;
      background: var(--ef-primary, #6f42c1);
      color: #fff;
      font-size: 1.125rem;
      box-shadow: 0 4px 12px rgba(0,0,0,0.2);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: fadeIn 0.2s ease;
      transition: background 0.15s ease, transform 0.15s ease;
    }
    .back-to-top-btn:hover {
      background: var(--ef-primary-dark, #5a32a3);
      transform: translateY(-2px);
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: scale(0.8); }
      to { opacity: 1; transform: scale(1); }
    }
  `
})
export class BackToTopComponent implements OnInit, OnDestroy {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  readonly visible = signal(false);
  private readonly onScroll = () => this.visible.set(window.scrollY > 400);

  ngOnInit(): void {
    if (this.isBrowser) {
      window.addEventListener('scroll', this.onScroll, { passive: true });
    }
  }

  ngOnDestroy(): void {
    if (this.isBrowser) {
      window.removeEventListener('scroll', this.onScroll);
    }
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
