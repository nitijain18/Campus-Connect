import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface VendorReport {
  id: number;
  reason: string;
  description: string;
  status: 'OPEN' | 'RESOLVED';
  reportedBy: any;
  event: any;
  vendor: any;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ReportService {
  private apiUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  create(report: any): Observable<VendorReport> {
    return this.http.post<VendorReport>(this.apiUrl, report);
  }

  getByStatus(status: 'OPEN' | 'RESOLVED'): Observable<VendorReport[]> {
    return this.http.get<VendorReport[]>(`${this.apiUrl}/status/${status}`);
  }

  resolve(id: number): Observable<VendorReport> {
    return this.http.patch<VendorReport>(`${this.apiUrl}/${id}/resolve`, {});
  }
}
