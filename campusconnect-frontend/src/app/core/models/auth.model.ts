export interface SignupRequest {
  firstName: string;
  lastName?: string;
  email: string;
  password: string;
  phoneNumber?: string;
  collegeId?: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface VerifyOtpRequest {
  email: string;
  otp: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  userId: number;
  email: string;
  role: 'ROLE_STUDENT' | 'ROLE_VENDOR';
  isAdmin: boolean;
  message: string;
}
