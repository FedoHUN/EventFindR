import { Routes } from '@angular/router';

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
    path: '**',
    redirectTo: ''
  }
];
