import { SessionOptions } from 'iron-session';

export interface UserSessionData {
  id: string;
  role: 'USER' | 'ADMIN';
  token: string; // JWT access token string for proxying to backend
  refreshToken?: string; // JWT refresh token for logout & token renewal
}

export interface SessionData {
  user?: UserSessionData;
}

export const sessionOptions: SessionOptions = {
  password: process.env.SECRET_COOKIE_PASSWORD || 'complex_password_at_least_32_characters_long',
  cookieName: 'simul_session',
  cookieOptions: {
    // HTTPS 환경이면 무조건 secure: true 적용
    secure: process.env.NODE_ENV === 'production',
    httpOnly: true,
    path: '/',
    sameSite: 'lax',
  },
};
