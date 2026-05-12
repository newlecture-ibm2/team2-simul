import { apiClient } from './client';

export interface ReportResponse {
  reportId: string;
  postId: string;
  reporterId: string;
  reason: string;
  createdAt: string;
}

import { PageResponse } from './feedAPI';

export const adminAPI = {
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
};
