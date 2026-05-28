import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  template: `
    <footer class="ef-footer">
      <div class="container">
        <div class="row g-4 pb-4">
          <div class="col-lg-4 col-md-6">
            <div class="d-flex align-items-center gap-2 mb-3">
              <span class="footer-brand-icon">
                <i class="bi bi-calendar-event"></i>
              </span>
              <span class="footer-brand-text">EventfindR</span>
            </div>
            <p class="footer-desc">
              Discover, track, and attend the best events in your area.
              Your go-to platform for live experiences.
            </p>
          </div>

          <div class="col-lg-2 col-md-6">
            <h6 class="footer-heading">Explore</h6>
            <ul class="list-unstyled footer-links">
              <li><a routerLink="/">Home</a></li>
              <li><a routerLink="/events">Events</a></li>
              <li><a routerLink="/about">About</a></li>
            </ul>
          </div>

          <div class="col-lg-3 col-md-6">
            <h6 class="footer-heading">For Organizers</h6>
            <ul class="list-unstyled footer-links">
              <li><a routerLink="/my-profile">Become an Organizer</a></li>
              <li><a routerLink="/events/create">Create Event</a></li>
            </ul>
          </div>

          <div class="col-lg-3 col-md-6">
            <h6 class="footer-heading">Contact</h6>
            <ul class="list-unstyled footer-links footer-contact">
              <li>
                <i class="bi bi-envelope"></i>
                <span>info&#64;eventfindr.sk</span>
              </li>
              <li>
                <i class="bi bi-geo-alt"></i>
                <span>Bratislava, Slovakia</span>
              </li>
            </ul>
          </div>
        </div>

        <div class="footer-bottom">
          <p class="mb-0">&copy; 2026 EventfindR. All rights reserved.</p>
        </div>
      </div>
    </footer>
  `,
  styles: `
    .ef-footer {
      background: linear-gradient(180deg, #111827 0%, #0a0e1a 100%);
      color: rgba(255, 255, 255, 0.7);
      padding-top: 3rem;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 500px;
        height: 1px;
        background: linear-gradient(
          90deg,
          transparent,
          rgba(139, 92, 246, 0.3),
          transparent
        );
      }
    }

    .footer-brand-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      border-radius: 6px;
      background: linear-gradient(135deg, var(--ef-primary-light), var(--ef-primary-dark));
      color: #fff;
      font-size: 0.875rem;
    }

    .footer-brand-text {
      font-family: 'Bricolage Grotesque', 'Segoe UI', sans-serif;
      font-weight: 700;
      font-size: 1.125rem;
      color: #fff;
    }

    .footer-desc {
      font-size: 0.875rem;
      line-height: 1.7;
      color: rgba(255, 255, 255, 0.5);
      max-width: 280px;
    }

    .footer-heading {
      font-size: 0.8125rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: rgba(255, 255, 255, 0.4);
      margin-bottom: 1rem;
    }

    .footer-links li {
      margin-bottom: 0.5rem;
    }

    .footer-links a {
      color: rgba(255, 255, 255, 0.65);
      font-size: 0.875rem;
      transition: color 0.15s ease;
    }

    .footer-links a:hover {
      color: #fff;
    }

    .footer-contact li {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: rgba(255, 255, 255, 0.5);
      font-size: 0.875rem;
    }

    .footer-contact i {
      font-size: 0.875rem;
      color: var(--ef-primary-light);
    }

    .footer-bottom {
      border-top: 1px solid rgba(255, 255, 255, 0.08);
      padding: 1.25rem 0;
      font-size: 0.8125rem;
      color: rgba(255, 255, 255, 0.35);
      text-align: center;
    }

    @media (max-width: 575.98px) {
      .ef-footer {
        padding-top: 2rem;
        overflow: hidden;
      }

      .footer-desc {
        font-size: 0.8rem;
      }

      .footer-heading {
        font-size: 0.75rem;
        margin-bottom: 0.625rem;
      }

      .footer-links a,
      .footer-contact li {
        font-size: 0.8rem;
      }
    }
  `
})
export class FooterComponent {}
