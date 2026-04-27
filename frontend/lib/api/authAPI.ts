import { apiClient } from './client';

/** 소셜 로그인 */
export async function socialLogin(provider: string, code: string) {
  return apiClient('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ provider, code }),
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
