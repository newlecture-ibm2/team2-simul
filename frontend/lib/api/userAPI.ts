import { apiClient } from './client';
import { User } from '@/lib/stores/useAuthStore';

/**
 * 타인 프로필 조회 (팔로워/팔로잉 수, 팔로우 여부 포함)
 */
export async function getUserProfile(userId: string) {
  return apiClient<User>(`/users/${userId}`);
}

/**
 * 팔로우
 */
export async function followUser(userId: string) {
  return apiClient(`/follows/${userId}`, { method: 'POST' });
}

/**
 * 언팔로우
 */
export async function unfollowUser(userId: string) {
  return apiClient(`/follows/${userId}`, { method: 'DELETE' });
}

/**
 * 팔로우 여부 확인
 */
export async function checkIsFollowing(userId: string) {
  return apiClient<{ isFollowing: boolean }>(`/users/${userId}/is-following`);
}

export interface FollowUserResponse {
  userId: string;
  nickname: string;
  profileImageUrl?: string;
  isFollowing: boolean;
}

/**
 * 팔로워 목록 조회
 */
export async function getFollowers(userId: string) {
  return apiClient<FollowUserResponse[]>(`/users/${userId}/followers`);
}

/**
 * 팔로잉 목록 조회
 */
export async function getFollowings(userId: string) {
  return apiClient<FollowUserResponse[]>(`/users/${userId}/followings`);
}
/**
 * 비밀번호 변경
 */
export async function changePassword(data: { oldPassword: string; newPassword: string }) {
  return apiClient('/users/me/password', {
    method: 'PATCH',
    body: JSON.stringify(data),
  });
}
