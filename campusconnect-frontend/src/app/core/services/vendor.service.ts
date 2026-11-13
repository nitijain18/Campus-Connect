import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Vendor } from '../models/vendor.model';

@Injectable({ providedIn: 'root' })
export class VendorService {
  private apiUrl = `${environment.apiUrl}/vendors`;

  constructor(private http: HttpClient) {}

  apply(vendor: Partial<Vendor>): Observable<Vendor> {
    return this.http.post<Vendor>(this.apiUrl, vendor);
  }

  getByUserId(userId: number): Observable<Vendor> {
    return this.http.get<Vendor>(`${this.apiUrl}/user/${userId}`);
  }

  getById(id: number): Observable<Vendor> {
    return this.http.get<Vendor>(`${this.apiUrl}/${id}`);
  }

  getPending(): Observable<Vendor[]> {
    return this.http.get<Vendor[]>(`${this.apiUrl}/pending`);
  }

  getApproved(): Observable<Vendor[]> {
    return this.http.get<Vendor[]>(`${this.apiUrl}/approved`);
  }

  approve(id: number): Observable<Vendor> {
    return this.http.patch<Vendor>(`${this.apiUrl}/${id}/approve`, {});
  }

  reject(id: number, reason: string): Observable<Vendor> {
    return this.http.patch<Vendor>(`${this.apiUrl}/${id}/reject`, reason, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  getEarnings(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}/earnings`);
  }
} 
