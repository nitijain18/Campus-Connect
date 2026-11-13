import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Event } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class EventService {
  private apiUrl = `${environment.apiUrl}/events`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Event[]> {
    return this.http.get<Event[]>(this.apiUrl);
  }

  getById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/${id}`);
  }

  search(params: any): Observable<Event[]> {
    let httpParams: any = {};
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') httpParams[key] = value;
    });
    return this.http.get<Event[]>(`${this.apiUrl}/search`, { params: httpParams });
  }

  getByVendor(vendorId: number): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/vendor/${vendorId}`);
  }

  getByCollege(collegeId: number): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/college/${collegeId}`);
  }

  getByCategory(categoryId: number): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/category/${categoryId}`);
  }

  create(event: Partial<Event>): Observable<Event> {
    return this.http.post<Event>(this.apiUrl, event);
  }

  update(id: number, event: Partial<Event>): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/${id}`, event);
  }

  remove(id: number, reason: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/remove`, reason, {
      headers: { 'Content-Type': 'text/plain' },
      responseType: 'text'
    });
  }
}
