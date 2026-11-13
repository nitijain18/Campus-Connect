import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Venue } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class VenueService {
  private apiUrl = `${environment.apiUrl}/venues`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Venue[]> {
    return this.http.get<Venue[]>(this.apiUrl);
  }

  getByCollege(collegeId: number): Observable<Venue[]> {
    return this.http.get<Venue[]>(`${this.apiUrl}/college/${collegeId}`);
  }

  create(venue: any): Observable<Venue> {
    return this.http.post<Venue>(this.apiUrl, venue);
  }
}
