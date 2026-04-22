import { Routes } from '@angular/router';
import { isLoggedIn } from './core/auth/auth-guards';

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
    path: 'events/:id',
    loadComponent: () => import('./modules/events/pages/event-detail/event-detail').then(m => m.EventDetailComponent)
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
    path: '**',
    redirectTo: ''
  }
];
