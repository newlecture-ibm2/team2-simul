import { apiClient } from './client';

export interface ReportResponse {
  reportId: string;
  postId: string;
  reporterId: string;
  reportedUserId?: string;
  reason: string;
  isBlinded: boolean;
  createdAt: string;
}

export interface AdminUserResponse {
  userId: string;
  nickname: string;
  providerId: string;
  role: string;
  provider: string;
  isActive: boolean;
  createdAt: string;
}

import { PageResponse } from './feedAPI';

export const adminAPI = {
  getUsers: async (page: number = 0, size: number = 20) => {
    return apiClient<PageResponse<AdminUserResponse>>('/admin/users', {
      params: { page, size },
    });
  },
  /**
   * 신고 목록 조회
   * @param page 페이지 번호 (0부터 시작)
   * @param size 페이지 크기
   * @returns 신고 목록 페이지
   */
  getReports: async (page: number = 0, size: number = 20) => {
    return apiClient<PageResponse<ReportResponse>>('/admin/reports', {
      params: { page, size },
    });
  },

  blindPost: async (postId: string) => {
    return apiClient(`/admin/posts/${postId}/blind`, { method: 'PATCH' });
  },

  unblindPost: async (postId: string) => {
    return apiClient(`/admin/posts/${postId}/unblind`, { method: 'PATCH' });
  },

  suspendUser: async (userId: string) => {
    return apiClient(`/admin/users/${userId}/suspend`, { method: 'PATCH' });
  },

  provideCredits: async (userId: string, amount: number) => {
    return apiClient(`/admin/users/${userId}/credits`, { 
      method: 'POST',
      data: { amount }
    });
  },
};
