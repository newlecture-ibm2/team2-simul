import client from './client';

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

export const notificationAPI = {
  /** 알림 목록 조회 */
  getNotifications: (page = 0, size = 20) =>
    client.get<any>(`/notifications?page=${page}&size=${size}`),

  /** 미읽음 알림 수 조회 */
  getUnreadCount: () =>
    client.get<{ unread_count: number }>('/notifications/unread-count'),

  /** 개별 읽음 처리 */
  markAsRead: (id: string) => client.patch(`/notifications/${id}/read`),

  /** 전체 읽음 처리 */
  markAllAsRead: () => client.patch<{ updated_count: number }>('/notifications/read-all'),
};
