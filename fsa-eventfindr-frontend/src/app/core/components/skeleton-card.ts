import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-skeleton-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="skeleton-card">
      <div class="skeleton-img shimmer"></div>
      <div class="skeleton-body">
        <div class="skeleton-badges">
          <div class="skeleton-line skeleton-sm shimmer" style="width:90px"></div>
          <div class="skeleton-line skeleton-sm shimmer" style="width:50px"></div>
        </div>
        <div class="skeleton-line shimmer" style="width:80%"></div>
        <div class="skeleton-line skeleton-sm shimmer" style="width:60%"></div>
        <div class="skeleton-line skeleton-sm shimmer" style="width:45%"></div>
      </div>
    </div>
  `,
  styles: `
    .skeleton-card {
      border-radius: var(--ef-radius-lg, 12px);
      overflow: hidden;
      background: var(--ef-surface);
      box-shadow: var(--ef-shadow-sm, 0 1px 3px rgba(0,0,0,0.08));
    }
    .skeleton-img {
      height: 200px;
      background: var(--ef-border-subtle);
    }
    .skeleton-body {
      padding: 1rem;
      display: flex;
      flex-direction: column;
      gap: 0.625rem;
    }
    .skeleton-badges {
      display: flex;
      gap: 0.5rem;
    }
    .skeleton-line {
      height: 14px;
      border-radius: 4px;
      background: var(--ef-border-subtle);
    }
    .skeleton-sm {
      height: 10px;
    }
    .shimmer {
      background: linear-gradient(90deg, var(--ef-border-subtle) 25%, var(--ef-border-light) 50%, var(--ef-border-subtle) 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
    }
    @keyframes shimmer {
      0% { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }
  `
})
export class SkeletonCardComponent {}
