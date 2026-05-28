import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { describe, expect, it, beforeEach } from 'vitest';
import { AuthService } from '../../../../core/auth/auth';
import { FollowApi } from '../../../profile/follow-api';
import { EventApi } from '../../event-api';
import { Event } from '../../event.model';
import { EventListComponent } from './event-list';

const events: Event[] = [
  {
    id: 1,
    name: 'Jazz Night',
    location: 'Bratislava',
    eventDate: '2099-01-01T20:00:00Z',
    genre: 'Jazz',
    artists: [{ id: 1, artistName: 'Blue Band', sortOrder: 0 }],
  },
  {
    id: 2,
    name: 'Rock Festival',
    location: 'Kosice',
    eventDate: '2099-02-01T20:00:00Z',
    genre: 'Rock',
    artists: [{ id: 2, artistName: 'Loud Group', sortOrder: 0 }],
  },
];

describe('EventListComponent', () => {
  let fixture: ComponentFixture<EventListComponent>;
  let component: EventListComponent;

  beforeEach(async () => {
    const anonymousUser = signal(undefined);

    await TestBed.configureTestingModule({
      imports: [EventListComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            user: anonymousUser.asReadonly(),
            isOrganizer: () => false,
          }
        },
        {
          provide: EventApi,
          useValue: {
            getAllEvents: () => of(events),
            resolveImageUrl: (url: string | undefined | null) => url ?? '',
          }
        },
        {
          provide: FollowApi,
          useValue: {
            getMyFollowing: () => of([]),
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EventListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads public events', () => {
    expect(component.filteredEvents().map(event => event.name)).toEqual(['Jazz Night', 'Rock Festival']);
  });

  it('filters events by search query', () => {
    component.searchControl.setValue('jazz');
    fixture.detectChanges();

    expect(component.filteredEvents().map(event => event.name)).toEqual(['Jazz Night']);
  });
});
