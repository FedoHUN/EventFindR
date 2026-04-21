import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-about',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <h1 class="fw-bold mb-4"><i class="bi bi-info-circle me-2"></i>About Us</h1>

          <div class="card border-0 shadow-sm mb-4">
            <div class="card-body p-4">
              <h3 class="fw-bold mb-3">What is EventfindR?</h3>
              <p class="fs-5 text-muted">
                EventfindR is a platform for discovering, tracking, and managing events.
                Whether you're looking for concerts, festivals, cultural gatherings, or community meetups,
                EventfindR helps you find what's happening near you and never miss an event that matters.
              </p>
              <p class="text-muted">
                Users can browse upcoming events freely without an account. By logging in,
                you can mark events as "Attending" or "Watching" to keep track of your plans.
                Organizers can create and manage their own events, making it easy to reach their audience.
              </p>
            </div>
          </div>

          <div class="card border-0 shadow-sm mb-4">
            <div class="card-body p-4">
              <h3 class="fw-bold mb-3">Features</h3>
              <div class="row g-3">
                <div class="col-md-6">
                  <div class="d-flex align-items-start">
                    <i class="bi bi-search text-primary fs-4 me-3 mt-1"></i>
                    <div>
                      <h6 class="fw-bold">Discover Events</h6>
                      <p class="text-muted small mb-0">Browse and filter upcoming events by name, location, or date.</p>
                    </div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="d-flex align-items-start">
                    <i class="bi bi-bookmark-check text-primary fs-4 me-3 mt-1"></i>
                    <div>
                      <h6 class="fw-bold">Track Attendance</h6>
                      <p class="text-muted small mb-0">Mark events as attending or watching to keep your plans organized.</p>
                    </div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="d-flex align-items-start">
                    <i class="bi bi-calendar-plus text-primary fs-4 me-3 mt-1"></i>
                    <div>
                      <h6 class="fw-bold">Organize Events</h6>
                      <p class="text-muted small mb-0">Create and manage your own events as an organizer.</p>
                    </div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="d-flex align-items-start">
                    <i class="bi bi-shield-lock text-primary fs-4 me-3 mt-1"></i>
                    <div>
                      <h6 class="fw-bold">Secure Access</h6>
                      <p class="text-muted small mb-0">Authentication powered by Keycloak with JWT tokens.</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="card border-0 shadow-sm mb-4">
            <div class="card-body p-4">
              <h3 class="fw-bold mb-3">Technology Stack</h3>
              <div class="row g-2">
                <div class="col-auto"><span class="badge bg-primary fs-6">Angular</span></div>
                <div class="col-auto"><span class="badge bg-primary fs-6">Spring Boot</span></div>
                <div class="col-auto"><span class="badge bg-primary fs-6">PostgreSQL</span></div>
                <div class="col-auto"><span class="badge bg-primary fs-6">Keycloak</span></div>
                <div class="col-auto"><span class="badge bg-primary fs-6">Bootstrap</span></div>
                <div class="col-auto"><span class="badge bg-primary fs-6">Docker</span></div>
                <div class="col-auto"><span class="badge bg-primary fs-6">Hexagonal Architecture</span></div>
              </div>
            </div>
          </div>

          <div class="card border-0 shadow-sm">
            <div class="card-body p-4">
              <h3 class="fw-bold mb-3">About the Creator</h3>
              <div class="d-flex align-items-center mb-3">
                <div class="rounded-circle bg-primary d-flex align-items-center justify-content-center me-3" style="width: 56px; height: 56px;">
                  <i class="bi bi-person-fill text-white fs-4"></i>
                </div>
                <div>
                  <h5 class="fw-bold mb-0">Mate Vojtko</h5>
                  <p class="text-muted mb-0">Developer &amp; Creator</p>
                </div>
              </div>
              <p class="text-muted">
                EventfindR was created as a full-stack application project, demonstrating
                modern web development practices including hexagonal architecture, RESTful APIs,
                and a responsive Angular frontend.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class AboutComponent {}
