import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ToastService } from '../services/toast';

@Component({
  selector: 'app-toast-container',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toast-container" aria-live="polite">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="ef-toast" [class]="'ef-toast--' + toast.type" role="alert">
          <i class="bi me-2" [class]="iconClass(toast.type)"></i>
          <span class="ef-toast-msg">{{ toast.message }}</span>
          <button class="ef-toast-close" (click)="toastService.dismiss(toast.id)" aria-label="Dismiss">&times;</button>
        </div>
      }
    </div>
  `,
  styles: `
    .toast-container {
      position: fixed;
      top: calc(var(--app-navbar-height, 60px) + 1rem);
      right: 1rem;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      max-width: 400px;
    }
    .ef-toast {
      display: flex;
      align-items: center;
      padding: 0.75rem 1rem;
      border-radius: 0.5rem;
      color: #fff;
      font-size: 0.875rem;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      animation: slideIn 0.3s ease;
    }
    .ef-toast--success { background: #059669; }
    .ef-toast--error { background: #dc2626; }
    .ef-toast--info { background: #6f42c1; }
    .ef-toast--warning { background: #d97706; }
    .ef-toast-msg { flex: 1; }
    .ef-toast-close {
      background: none;
      border: none;
      color: rgba(255,255,255,0.8);
      font-size: 1.25rem;
      cursor: pointer;
      padding: 0 0 0 0.5rem;
      line-height: 1;
    }
    .ef-toast-close:hover { color: #fff; }
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `
})
export class ToastContainerComponent {
  readonly toastService = inject(ToastService);

  iconClass(type: string): string {
    switch (type) {
      case 'success': return 'bi-check-circle-fill';
      case 'error': return 'bi-exclamation-triangle-fill';
      case 'warning': return 'bi-exclamation-circle-fill';
      default: return 'bi-info-circle-fill';
    }
  }
}
