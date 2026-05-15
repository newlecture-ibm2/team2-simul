import { apiClient } from './client';
import { User } from '@/lib/stores/useAuthStore';

/** 소셜 로그인 */
export async function socialLogin(provider: string, code: string, redirectUri: string) {
  return apiClient<{ accessToken: string; refreshToken: string; isNewUser: boolean; success: boolean }>('/auth/social', {
    method: 'POST',
    body: JSON.stringify({ provider, code, redirectUri }),
  });
}

export async function emailSignup(data: Record<string, unknown>) {
  return apiClient<{ accessToken: string; refreshToken: string; isNewUser: boolean; success: boolean }>('/auth/signup', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function emailLogin(data: Record<string, unknown>) {
  return apiClient<{ accessToken: string; refreshToken: string; isNewUser: boolean; success: boolean }>('/auth/login/email', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/** 계정 복구 */
export async function restoreAccount(provider: string, providerId: string) {
  return apiClient<{ accessToken: string; refreshToken: string; isNewUser: boolean; success: boolean }>('/auth/restore', {
    method: 'POST',
    body: JSON.stringify({ provider, providerId }),
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
export async function updateProfile(data: { 
  nickname?: string; 
  bio?: string; 
  profileImage?: File; 
  bannerImage?: File;
  profileImageUrl?: string;
  bannerImageUrl?: string;
}) {
  const formData = new FormData();
  
  // JSON 데이터 (data 파트)
  const userData = {
    nickname: data.nickname,
    bio: data.bio,
    profileImageUrl: data.profileImageUrl,
    bannerImageUrl: data.bannerImageUrl
  };
  formData.append('data', new Blob([JSON.stringify(userData)], { type: 'application/json' }));
  
  // 이미지 파일들
  if (data.profileImage) {
    formData.append('profileImage', data.profileImage);
  }
  if (data.bannerImage) {
    formData.append('bannerImage', data.bannerImage);
  }

  return apiClient('/users/me', {
    method: 'PATCH',
    body: formData,
  });
}

/** 회원 탈퇴 */
export async function withdraw() {
  return apiClient('/users/me', {
    method: 'DELETE',
  });
}
