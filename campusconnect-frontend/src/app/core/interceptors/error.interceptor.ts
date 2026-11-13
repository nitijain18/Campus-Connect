import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthEndpoint = req.url.includes('/users/login') || req.url.includes('/users/refresh-token');

      if (error.status === 401 && !isAuthEndpoint && authService.getRefreshToken()) {
        // Access token expired - try refreshing it once, then retry the original request
        return authService.refreshAccessToken().pipe(
          switchMap(() => {
            const newToken = authService.getToken();
            const retriedReq = req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}` }
            });
            return next(retriedReq);
          }),
          catchError(() => {
            // Refresh token itself is invalid/expired - force logout
            authService.logout();
            return throwError(() => error);
          })
        );
      }

      if (error.status === 401) {
        authService.logout();
      }

      return throwError(() => error);
    })
  );
};
