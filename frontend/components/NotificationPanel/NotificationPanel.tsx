'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import styles from './NotificationPanel.module.css';
import { notificationAPI, NotificationResponse } from '@/lib/api/notificationAPI';
import { useNotificationStore } from '@/lib/stores/useNotificationStore';

/** 알림 유형별 이동 경로 결정 */
function getNotificationUrl(notification: NotificationResponse): string | null {
  const { type, referenceId } = notification;
  if (!referenceId) return null;

  switch (type) {
    case 'LIKE':
    case 'COMMENT':
    case 'FOLLOW_POST':
      return `/post/${referenceId}`;
    case 'TRYON_COMPLETE':
      return `/tryon/result?jobId=${referenceId}`;
    default:
      return null;
  }
}

/** 알림 유형별 아이콘 매핑 */
const NOTIFICATION_ICONS: Record<NotificationResponse['type'], string> = {
  TRYON_COMPLETE: '/icons/tryon.png',
  LIKE: '/icons/heart.png',
  COMMENT: '/icons/bubble.png',
  FOLLOW_POST: '/icons/rectangle.portrait.on.rectangle.portrait.angled.png',
};

/** 알림 유형별 라벨 */
const NOTIFICATION_LABELS: Record<NotificationResponse['type'], string> = {
  TRYON_COMPLETE: '시착 완료',
  LIKE: '좋아요',
  COMMENT: '댓글',
  FOLLOW_POST: '새 게시물',
};

interface NotificationPanelProps {
  isOpen: boolean;
  onClose: () => void;
}

/** 상대 시간 포맷 */
function formatRelativeTime(dateString: string): string {
  const now = new Date();
  const date = new Date(dateString);
  const diffMs = now.getTime() - date.getTime();
  const diffMin = Math.floor(diffMs / 60000);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffMin < 1) return '방금 전';
  if (diffMin < 60) return `${diffMin}분 전`;
  if (diffHour < 24) return `${diffHour}시간 전`;
  if (diffDay < 7) return `${diffDay}일 전`;
  return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
}

export default function NotificationPanel({ isOpen, onClose }: NotificationPanelProps) {
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const { setUnreadCount, clearUnreadCount } = useNotificationStore();
  const router = useRouter();

  /** 알림 목록 가져오기 */
  const fetchNotifications = useCallback(async () => {
    setLoading(true);
    try {
      const res = await notificationAPI.getNotifications(0, 20);
      setNotifications(res.content);
    } catch (err) {
      console.error('알림 목록 조회 실패:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  /** 패널 열릴 때 알림 목록 로드 */
  useEffect(() => {
    if (isOpen) {
      fetchNotifications();
    }
  }, [isOpen, fetchNotifications]);

  /** 전체 읽음 처리 */
  const handleMarkAllAsRead = async () => {
    try {
      await notificationAPI.markAllAsRead();
      // 로컬 상태 업데이트
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      clearUnreadCount();
    } catch (err) {
      console.error('전체 읽음 처리 실패:', err);
    }
  };

  /** 개별 읽음 처리 */
  const handleMarkAsRead = async (notificationId: string) => {
    try {
      await notificationAPI.markAsRead(notificationId);
      setNotifications(prev =>
        prev.map(n =>
          n.notificationId === notificationId ? { ...n, isRead: true } : n
        )
      );
      // 미읽음 수 갱신
      const res = await notificationAPI.getUnreadCount();
      setUnreadCount(res.unread_count);
    } catch (err) {
      console.error('읽음 처리 실패:', err);
    }
  };

  /** 알림 클릭: 읽음 처리 + 관련 페이지로 이동 */
  const handleNotificationClick = async (notification: NotificationResponse) => {
    // 미읽음이면 읽음 처리
    if (!notification.isRead) {
      handleMarkAsRead(notification.notificationId);
    }

    // 관련 페이지로 이동
    const url = getNotificationUrl(notification);
    if (url) {
      onClose(); // 패널 닫기
      router.push(url);
    }
  };

  if (!isOpen) return null;

  const unreadExists = notifications.some(n => !n.isRead);

  return (
    <>
      {/* 배경 오버레이 (클릭 시 닫기) */}
      <div className={styles.overlay} onClick={onClose} />

      {/* 알림 패널 */}
      <div className={styles.panel}>
        {/* 헤더 영역 */}
        <div className={styles.panelHeader}>
          <h3 className={styles.panelTitle}>알림</h3>
          <div className={styles.panelActions}>
            {unreadExists && (
              <button className={styles.markAllBtn} onClick={handleMarkAllAsRead}>
                모두 읽음
              </button>
            )}
            <button className={styles.closeBtn} onClick={onClose} aria-label="닫기">
              ✕
            </button>
          </div>
        </div>

        {/* 알림 목록 */}
        <div className={styles.notificationList}>
          {loading ? (
            <div className={styles.emptyState}>
              <p>불러오는 중...</p>
            </div>
          ) : notifications.length === 0 ? (
            <div className={styles.emptyState}>
              <img src="/icons/ring.png" alt="" className={styles.emptyIcon} />
              <p>새로운 알림이 없습니다</p>
            </div>
          ) : (
            notifications.map((notification) => (
              <button
                key={notification.notificationId}
                className={`${styles.notificationItem} ${!notification.isRead ? styles.unread : ''}`}
                onClick={() => handleNotificationClick(notification)}
              >
                {/* 유형별 아이콘 */}
                <div className={styles.iconContainer}>
                  <img
                    src={NOTIFICATION_ICONS[notification.type]}
                    alt={NOTIFICATION_LABELS[notification.type]}
                    className={styles.typeIcon}
                  />
                </div>

                {/* 내용 */}
                <div className={styles.itemContent}>
                  <span className={styles.typeLabel}>
                    {NOTIFICATION_LABELS[notification.type]}
                  </span>
                  <p className={styles.itemMessage}>{notification.message}</p>
                  <span className={styles.itemTime}>
                    {formatRelativeTime(notification.createdAt)}
                  </span>
                </div>

                {/* 미읽음 인디케이터 */}
                {!notification.isRead && (
                  <div className={styles.unreadDot} />
                )}
              </button>
            ))
          )}
        </div>
      </div>
    </>
  );
}
