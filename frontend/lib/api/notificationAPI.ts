import { apiClient } from './client';

export interface NotificationResponse {
  notificationId: string;
  recipientId: string;
  actorId: string | null;
  type: 'TRYON_COMPLETE' | 'LIKE' | 'COMMENT' | 'FOLLOW_POST';
  referenceId: string | null;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationPageResponse {
  content: NotificationResponse[];
  totalPages: number;
  totalElements: number;
  last: boolean;
  size: number;
  number: number;
}

export const notificationAPI = {
  /** 알림 목록 조회 */
  getNotifications: (page = 0, size = 20) =>
    apiClient<NotificationPageResponse>(`/notifications?page=${page}&size=${size}`),

  /** 미읽음 알림 수 조회 */
  getUnreadCount: () =>
    apiClient<{ unread_count: number }>('/notifications/unread-count'),

  /** 개별 읽음 처리 */
  markAsRead: (id: string) => apiClient<void>(`/notifications/${id}/read`, { method: 'PATCH' }),

  /** 전체 읽음 처리 */
  markAllAsRead: () => apiClient<{ updated_count: number }>('/notifications/read-all', { method: 'PATCH' }),
};
