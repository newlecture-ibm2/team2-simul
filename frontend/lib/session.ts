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
    // In local http environments (including docker-compose), secure cookies won't be stored/sent.
    // Allow explicit override via SESSION_COOKIE_SECURE=true/false.
    secure:
      process.env.SESSION_COOKIE_SECURE != null
        ? process.env.SESSION_COOKIE_SECURE === 'true'
        : process.env.NODE_ENV === 'production' && (process.env.APP_URL?.startsWith('https://') ?? false),
  },
};
