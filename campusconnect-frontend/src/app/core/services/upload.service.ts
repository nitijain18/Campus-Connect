import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Event } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class UploadService {
  private apiUrl = `${environment.apiUrl}/upload`;

  constructor(private http: HttpClient) {}

  uploadEventImages(eventId: number, files: File[]): Observable<Event> {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));
    return this.http.post<Event>(`${this.apiUrl}/event/${eventId}/images`, formData);
  }
}
