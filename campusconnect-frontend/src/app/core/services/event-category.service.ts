import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EventCategory } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class EventCategoryService {
  private apiUrl = `${environment.apiUrl}/event-categories`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<EventCategory[]> {
    return this.http.get<EventCategory[]>(this.apiUrl);
  }

  create(category: any): Observable<EventCategory> {
    return this.http.post<EventCategory>(this.apiUrl, category);
  }
}
