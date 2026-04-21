import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  template: `
    <footer class="bg-dark text-light py-4 mt-auto">
      <div class="container">
        <div class="row">
          <div class="col-md-4 mb-3 mb-md-0">
            <h5 class="fw-bold"><i class="bi bi-calendar-event me-2"></i>EventfindR</h5>
            <p class="text-secondary small">Discover and track events that matter to you.</p>
          </div>
          <div class="col-md-4 mb-3 mb-md-0">
            <h6>Quick Links</h6>
            <ul class="list-unstyled">
              <li><a routerLink="/" class="text-secondary">Home</a></li>
              <li><a routerLink="/events" class="text-secondary">Events</a></li>
              <li><a routerLink="/about" class="text-secondary">About Us</a></li>
            </ul>
          </div>
          <div class="col-md-4">
            <h6>Contact</h6>
            <p class="text-secondary small mb-1"><i class="bi bi-envelope me-1"></i>info&#64;eventfindr.sk</p>
            <p class="text-secondary small"><i class="bi bi-geo-alt me-1"></i>Slovakia</p>
          </div>
        </div>
        <hr class="border-secondary">
        <p class="text-center text-secondary small mb-0">&copy; 2026 EventfindR. All rights reserved.</p>
      </div>
    </footer>
  `
})
export class FooterComponent {}
