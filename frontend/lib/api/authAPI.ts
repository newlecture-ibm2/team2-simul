import { apiClient } from './client';

/** 소셜 로그인 */
export async function socialLogin(provider: string, code: string, redirectUri: string) {
  return apiClient('/auth/social', {
    method: 'POST',
    body: JSON.stringify({ provider, code, redirectUri }),
  });
}

export async function emailSignup(data: Record<string, unknown>) {
  return apiClient('/auth/email', {
    method: 'POST',
    body: JSON.stringify({ ...data, type: 'signup' }),
  });
}

export async function emailLogin(data: Record<string, unknown>) {
  return apiClient('/auth/email', {
    method: 'POST',
    body: JSON.stringify({ ...data, type: 'login' }),
  });
}

/** 로그아웃 */
export async function logout() {
  return apiClient('/auth/logout', { method: 'POST' });
}

/** 현재 유저 정보 조회 */
export async function getCurrentUser() {
  return apiClient('/auth/me');
}
