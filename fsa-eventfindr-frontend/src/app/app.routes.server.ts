import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: '',
    renderMode: RenderMode.Client
  },
  {
    path: 'events',
    renderMode: RenderMode.Client
  },
  {
    path: 'events/:id',
    renderMode: RenderMode.Client
  },
  {
    path: 'about',
    renderMode: RenderMode.Prerender
  },
  {
    path: '**',
    renderMode: RenderMode.Client
  }
];
