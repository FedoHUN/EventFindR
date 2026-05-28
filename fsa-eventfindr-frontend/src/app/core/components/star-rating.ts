import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';

@Component({
  selector: 'app-star-rating',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '[class]': '"star-rating--" + size()' },
  template: `
    <span class="star-rating" [class.star-rating--interactive]="interactive()" role="group" [attr.aria-label]="ariaLabel()">
      @for (star of stars; track star) {
        @if (interactive()) {
          <button
            type="button"
            class="star-btn"
            [class.star-filled]="star <= displayValue()"
            [class.star-hovered]="hoveredStar() >= star"
            (mouseenter)="hoveredStar.set(star)"
            (mouseleave)="hoveredStar.set(0)"
            (click)="onStarClick(star)"
            [attr.aria-label]="star + ' star' + (star > 1 ? 's' : '')">
            <i class="bi" [class.bi-star-fill]="star <= displayValue()" [class.bi-star]="star > displayValue()"></i>
          </button>
        } @else {
          <i class="bi star-icon"
            [class.bi-star-fill]="star <= fullStars()"
            [class.bi-star-half]="star === fullStars() + 1 && hasHalfStar()"
            [class.bi-star]="star > fullStars() + (hasHalfStar() ? 1 : 0)"
            [class.star-filled]="star <= fullStars() || (star === fullStars() + 1 && hasHalfStar())">
          </i>
        }
      }
      @if (showValue() && value()) {
        <span class="star-value">{{ value()!.toFixed(1) }}</span>
      }
      @if (showCount() && count() > 0) {
        <span class="star-count">({{ count() }})</span>
      }
    </span>
  `,
  styles: `
    .star-rating {
      display: inline-flex;
      align-items: center;
      gap: 1px;
      line-height: 1;
    }
    .star-icon {
      color: #d1d5db;
      font-size: inherit;
    }
    .star-icon.star-filled {
      color: #f59e0b;
    }
    .star-btn {
      background: none;
      border: none;
      padding: 0 1px;
      cursor: pointer;
      color: #d1d5db;
      font-size: inherit;
      line-height: 1;
      transition: transform 0.15s ease;
    }
    .star-btn:hover {
      transform: scale(1.15);
    }
    .star-btn.star-filled,
    .star-btn.star-hovered {
      color: #f59e0b;
    }
    .star-value {
      font-weight: 700;
      font-size: 0.85em;
      margin-left: 4px;
      color: #374151;
    }
    .star-count {
      font-size: 0.8em;
      color: #9ca3af;
      margin-left: 2px;
    }
    :host(.star-rating--sm) { font-size: 0.75rem; }
    :host(.star-rating--md) { font-size: 1rem; }
    :host(.star-rating--lg) { font-size: 1.25rem; }
  `
})
export class StarRatingComponent {
  readonly value = input<number | null>(null);
  readonly count = input(0);
  readonly interactive = input(false);
  readonly showValue = input(false);
  readonly showCount = input(false);
  readonly size = input<'sm' | 'md' | 'lg'>('md');
  readonly ratingChange = output<number>();

  readonly hoveredStar = signal(0);
  readonly selectedStar = signal(0);

  readonly stars = [1, 2, 3, 4, 5];

  readonly fullStars = computed(() => Math.floor(this.value() ?? 0));
  readonly hasHalfStar = computed(() => {
    const v = this.value() ?? 0;
    return v - Math.floor(v) >= 0.25;
  });

  readonly displayValue = computed(() =>
    this.hoveredStar() > 0 ? this.hoveredStar() : this.selectedStar()
  );

  readonly ariaLabel = computed(() => {
    if (this.interactive()) return 'Rate this event';
    const v = this.value();
    return v ? `Rating: ${v.toFixed(1)} out of 5` : 'No rating';
  });

  onStarClick(star: number): void {
    if (this.selectedStar() === star) {
      this.selectedStar.set(0);
      this.ratingChange.emit(0);
    } else {
      this.selectedStar.set(star);
      this.ratingChange.emit(star);
    }
  }
}
