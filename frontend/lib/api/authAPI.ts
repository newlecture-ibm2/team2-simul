import { apiClient } from './client';
import { User } from '@/lib/stores/useAuthStore';

/** 소셜 로그인 */
export async function socialLogin(provider: string, code: string, redirectUri: string) {
  return apiClient<{ user: User; isNewUser: boolean }>('/auth/social', {
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
  return apiClient<User>('/users/me');
}

/** 프로필 수정 */
export async function updateProfile(data: Record<string, unknown>) {
  return apiClient('/users/me', {
    method: 'PATCH',
    body: JSON.stringify(data),
  });
}

/** 회원 탈퇴 */
export async function withdraw() {
  return apiClient('/users/me', {
    method: 'DELETE',
  });
}
