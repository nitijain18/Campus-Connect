import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HomeCategory } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class HomeCategoryService {
  private apiUrl = `${environment.apiUrl}/home-categories`;

  constructor(private http: HttpClient) {}

  getActive(): Observable<HomeCategory[]> {
    return this.http.get<HomeCategory[]>(`${this.apiUrl}/active`);
  }
}
