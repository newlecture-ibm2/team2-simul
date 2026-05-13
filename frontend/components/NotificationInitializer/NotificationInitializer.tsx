'use client';

import { useEffect } from 'react';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import { toast } from '@/lib/utils/toast';
import { NotificationResponse } from '@/lib/api/notificationAPI';
import { useNotificationStore } from '@/lib/stores/useNotificationStore';

/**
 * 실시간 알림 초기화 컴포넌트
 * - 로그인 상태일 때 백엔드와 SSE 연결을 맺고 실시간 알림을 수신함
 */
export function NotificationInitializer() {
  const { isAuthenticated } = useAuthStore();
  const { incrementUnreadCount } = useNotificationStore();

  useEffect(() => {
    // 로그인 상태가 아닐 때는 구독하지 않음
    if (!isAuthenticated) return;

    // BFF를 통해 SSE 구독 (URL에 직접 접근 시 403을 방지하기 위해 BFF 프록시 활용)
    const eventSource = new EventSource('/api/notifications/subscribe');

    // 연결 성공 시
    eventSource.onopen = () => {
      console.log('[SSE] 알림 서비스 연결 성공');
    };

    // 새 알림 수신 시
    eventSource.onmessage = (event) => {
      try {
        const data: NotificationResponse = JSON.parse(event.data);
        console.log('[SSE] 새 알림 수신:', data);

        // 미읽음 카운트 증가
        incrementUnreadCount();

        // 알림 메시지를 Toast로 노출
        // 시착 완료("TRYON_COMPLETE") 등 모든 알림에 대해 Toast를 띄움
        toast.success(data.message);
      } catch (err) {
        console.error('[SSE] 메시지 파싱 에러:', err);
      }
    };

    // 연결 에러 발생 시 처리 (브라우저가 자동으로 재연결을 시도함)
    eventSource.onerror = (err) => {
      console.error('[SSE] 알림 서비스 연결 에러:', err);
    };

    // 클린업: 컴포넌트 언마운트 또는 로그인 상태 변경 시 연결 종료
    return () => {
      console.log('[SSE] 알림 서비스 연결 해제');
      eventSource.close();
    };
  }, [isAuthenticated, incrementUnreadCount]);

  return null; // UI 없이 로직만 수행
}
