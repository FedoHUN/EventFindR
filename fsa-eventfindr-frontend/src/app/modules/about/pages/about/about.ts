import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-about',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-lg-8">

          <!-- Header -->
          <div class="about-header text-center mb-5">
            <span class="about-badge">
              <i class="bi bi-info-circle me-1"></i>About Us
            </span>
            <h1 class="about-title">EventfindR</h1>
            <p class="about-subtitle">
              A production-grade event platform for discovery, organizers, artists, and communities — built full-stack from database to cloud.
            </p>
          </div>

          <!-- What is EventfindR -->
          <div class="about-card mb-4">
            <div class="about-card-icon">
              <i class="bi bi-lightning-charge-fill"></i>
            </div>
            <h3 class="about-card-title">What is EventfindR?</h3>
            <p>
              EventfindR is a modern event discovery and management platform.
              Visitors can browse upcoming and trending events, view organizer and artist profiles,
              explore media galleries, read ratings and comments, and discover similar events — all without an account.
            </p>
            <p class="mb-0 text-muted">
              Authenticated users unlock a personal event calendar, attendance and watch-list tracking,
              real-time notifications, following organizers, and the ability to rate and comment on events.
              Organizers manage the full event lifecycle — draft, publish, cancel, restore — upload media galleries,
              set capacity limits, assign artists, and publish posts to keep their audience engaged.
            </p>
          </div>

          <!-- Features -->
          <div class="about-card mb-4">
            <h3 class="about-card-title">Features</h3>
            <div class="row g-4 mt-1">
              <div class="col-md-6">
                <div class="feature-item">
                  <div class="feature-icon">
                    <i class="bi bi-search"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold mb-1">Smart Discovery</h6>
                    <p class="text-muted small mb-0">Filter by genre, location, date, artist, and sort by date, rating, or popularity. Trending and similar event recommendations.</p>
                  </div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="feature-item">
                  <div class="feature-icon">
                    <i class="bi bi-bookmark-check"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold mb-1">Personal Calendar</h6>
                    <p class="text-muted small mb-0">Attend or watch events, track your schedule, and manage your upcoming calendar in one place.</p>
                  </div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="feature-item">
                  <div class="feature-icon">
                    <i class="bi bi-calendar-plus"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold mb-1">Organizer Tools</h6>
                    <p class="text-muted small mb-0">Draft-to-publish workflow, media gallery uploads, capacity management, featured events, cancel/restore lifecycle, and follower notifications.</p>
                  </div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="feature-item">
                  <div class="feature-icon">
                    <i class="bi bi-music-note-beamed"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold mb-1">Artists & Profiles</h6>
                    <p class="text-muted small mb-0">Dedicated artist profiles, performance history, organizer pages with posts, and follower relationships.</p>
                  </div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="feature-item">
                  <div class="feature-icon">
                    <i class="bi bi-chat-square-heart"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold mb-1">Community Engagement</h6>
                    <p class="text-muted small mb-0">Comments with star ratings, event attendance counters, real-time notification system, and sharing.</p>
                  </div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="feature-item">
                  <div class="feature-icon">
                    <i class="bi bi-images"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold mb-1">Rich Media</h6>
                    <p class="text-muted small mb-0">Image and video galleries per event, post media uploads, progress-tracked upload transport, and reorderable media.</p>
                  </div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="feature-item">
                  <div class="feature-icon">
                    <i class="bi bi-shield-lock"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold mb-1">Secure Architecture</h6>
                    <p class="text-muted small mb-0">Keycloak OAuth2, JWT validation, role-based access control, scoped token attachment, and backend domain authorization.</p>
                  </div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="feature-item">
                  <div class="feature-icon">
                    <i class="bi bi-cloud-check"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold mb-1">Cloud-Native Deployment</h6>
                    <p class="text-muted small mb-0">Dockerized services, Kubernetes workloads on Azure AKS, Helm charts, cert-manager TLS, and CI/CD pipeline.</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tech Stack -->
          <div class="about-card mb-4">
            <h3 class="about-card-title">Technology Stack</h3>
            <div class="d-flex flex-wrap gap-2 mt-3">
              <span class="tech-badge"><i class="bi bi-code-square me-1"></i>Angular 21</span>
              <span class="tech-badge"><i class="bi bi-lightning-charge me-1"></i>Signals</span>
              <span class="tech-badge"><i class="bi bi-filetype-tsx me-1"></i>TypeScript 5.9</span>
              <span class="tech-badge"><i class="bi bi-server me-1"></i>Spring Boot 4</span>
              <span class="tech-badge"><i class="bi bi-cup-hot me-1"></i>Java 21</span>
              <span class="tech-badge"><i class="bi bi-database me-1"></i>PostgreSQL</span>
              <span class="tech-badge"><i class="bi bi-key me-1"></i>Keycloak 26</span>
              <span class="tech-badge"><i class="bi bi-palette me-1"></i>Bootstrap 5</span>
              <span class="tech-badge"><i class="bi bi-box me-1"></i>Docker</span>
              <span class="tech-badge"><i class="bi bi-cloud me-1"></i>Azure AKS</span>
              <span class="tech-badge"><i class="bi bi-diagram-3 me-1"></i>Hexagonal Architecture</span>
              <span class="tech-badge"><i class="bi bi-gear me-1"></i>Helm &amp; Kubernetes</span>
              <span class="tech-badge"><i class="bi bi-droplet me-1"></i>Liquibase</span>
              <span class="tech-badge"><i class="bi bi-map me-1"></i>MapStruct</span>
            </div>
          </div>

          <!-- Creator -->
          <div class="about-card">
            <h3 class="about-card-title">About the Creator</h3>
            <div class="creator-card mt-3">
              <div class="creator-avatar">
                <i class="bi bi-person-fill"></i>
              </div>
              <div>
                <h5 class="fw-bold mb-1">M&aacute;t&eacute; Vojtko</h5>
                <p class="text-muted small mb-2">Full-Stack Developer</p>
                <p class="text-muted small mb-0">
                  EventfindR was designed and built end-to-end as a production-grade demonstration of
                  modern full-stack development: Angular standalone architecture with signals, a Spring Boot hexagonal backend
                  with clean domain separation, Keycloak-secured authentication, rich media handling,
                  role-based organizer and artist workflows, and cloud-native deployment on Azure Kubernetes Service.
                </p>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  `,
  styles: `
    .about-header {
      padding-top: 1rem;
    }

    .about-badge {
      display: inline-block;
      padding: 0.375rem 1rem;
      border-radius: 2rem;
      background: var(--ef-primary-50);
      color: var(--ef-primary);
      font-size: 0.8125rem;
      font-weight: 600;
      margin-bottom: 1rem;
    }

    .about-title {
      font-size: 2.75rem;
      font-weight: 800;
      letter-spacing: -0.035em;
      margin-bottom: 0.5rem;
      background: linear-gradient(135deg, var(--ef-primary), var(--ef-accent));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .about-subtitle {
      font-size: 1.125rem;
      color: var(--ef-text-muted);
      max-width: 480px;
      margin-inline: auto;
    }

    .about-card {
      background: var(--ef-surface);
      border: 1px solid var(--ef-border-subtle);
      border-radius: var(--ef-radius-lg);
      padding: 2rem;
      box-shadow: var(--ef-shadow-xs);
    }

    .about-card-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      background: var(--ef-primary-50);
      color: var(--ef-primary);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
      margin-bottom: 1rem;
    }

    .about-card-title {
      font-size: 1.25rem;
      font-weight: 800;
      margin-bottom: 0.75rem;
    }

    .feature-item {
      display: flex;
      gap: 0.75rem;
      align-items: flex-start;
    }

    .feature-icon {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      background: var(--ef-primary-50);
      color: var(--ef-primary);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.875rem;
      flex-shrink: 0;
    }

    .tech-badge {
      display: inline-flex;
      align-items: center;
      padding: 0.4rem 0.875rem;
      border-radius: 2rem;
      background: var(--ef-primary-50);
      color: var(--ef-primary);
      font-size: 0.8125rem;
      font-weight: 600;
      transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    .tech-badge:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(111, 66, 193, 0.12);
    }

    .creator-card {
      display: flex;
      gap: 1rem;
      align-items: flex-start;
      padding: 1.25rem;
      background: var(--ef-primary-50);
      border-radius: var(--ef-radius-md);
    }

    .creator-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--ef-primary-dark), var(--ef-primary-light));
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 1.25rem;
      flex-shrink: 0;
    }

    @media (max-width: 575.98px) {
      .about-header {
        margin-bottom: 2rem !important;
      }

      .about-title {
        font-size: 2.1rem;
      }

      .about-subtitle {
        font-size: 1rem;
      }

      .about-card {
        padding: 1.25rem;
        border-radius: var(--ef-radius-md);
      }

      .creator-card {
        padding: 1rem;
      }
    }
  `
})
export class AboutComponent {}
