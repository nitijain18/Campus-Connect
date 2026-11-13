import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { College } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class CollegeService {
  private apiUrl = `${environment.apiUrl}/colleges`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<College[]> {
    return this.http.get<College[]>(this.apiUrl);
  }

  getById(id: number): Observable<College> {
    return this.http.get<College>(`${this.apiUrl}/${id}`);
  }
}
