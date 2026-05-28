import { Routes } from '@angular/router';
import { isLoggedIn, isOrganizer } from './core/auth/auth-guards';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./modules/home/pages/home/home').then(m => m.HomeComponent)
  },
  {
    path: 'events',
    loadComponent: () => import('./modules/events/pages/event-list/event-list').then(m => m.EventListComponent)
  },
  {
    path: 'events/my',
    canActivate: [isOrganizer],
    loadComponent: () => import('./modules/events/pages/my-events/my-events').then(m => m.MyEventsComponent)
  },
  {
    path: 'events/create',
    canActivate: [isOrganizer],
    loadComponent: () => import('./modules/events/pages/event-create/event-create').then(m => m.EventCreateComponent)
  },
  {
    path: 'events/:id/edit',
    canActivate: [isOrganizer],
    loadComponent: () => import('./modules/events/pages/event-edit/event-edit').then(m => m.EventEditComponent)
  },
  {
    path: 'events/:id',
    loadComponent: () => import('./modules/events/pages/event-detail/event-detail').then(m => m.EventDetailComponent)
  },
  {
    path: 'my-calendar',
    canActivate: [isLoggedIn],
    loadComponent: () => import('./modules/events/pages/my-calendar/my-calendar').then(m => m.MyCalendarComponent)
  },
  {
    path: 'discover',
    loadComponent: () => import('./modules/discover/pages/discover/discover').then(m => m.DiscoverComponent)
  },
  {
    path: 'about',
    loadComponent: () => import('./modules/about/pages/about/about').then(m => m.AboutComponent)
  },
  {
    path: 'my-profile',
    canActivate: [isLoggedIn],
    loadComponent: () => import('./modules/profile/pages/my-profile/my-profile').then(m => m.MyProfileComponent)
  },
  {
    path: 'profile/:id',
    loadComponent: () => import('./modules/profile/pages/public-profile/public-profile').then(m => m.PublicProfileComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
