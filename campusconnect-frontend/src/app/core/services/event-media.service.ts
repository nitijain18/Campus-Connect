import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EventMedia } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class EventMediaService {
  private apiUrl = `${environment.apiUrl}/event-media`;

  constructor(private http: HttpClient) {}

  getByEvent(eventId: number): Observable<EventMedia[]> {
    return this.http.get<EventMedia[]>(`${this.apiUrl}/event/${eventId}`);
  }
}
