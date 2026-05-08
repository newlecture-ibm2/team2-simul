import { SessionOptions } from 'iron-session';

export interface UserSessionData {
  id: string;
  role: 'USER' | 'ADMIN';
  token: string; // JWT token string for proxying to backend
}

export interface SessionData {
  user?: UserSessionData;
}

export const sessionOptions: SessionOptions = {
  password: process.env.SECRET_COOKIE_PASSWORD || 'complex_password_at_least_32_characters_long',
  cookieName: 'simul_session',
  cookieOptions: {
    secure: process.env.NODE_ENV === 'production',
  },
};
