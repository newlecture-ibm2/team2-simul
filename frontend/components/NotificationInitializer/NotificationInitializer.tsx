'use client';

import { useEffect } from 'react';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import { toast } from '@/lib/utils/toast';
import { NotificationResponse } from '@/lib/api/notificationAPI';

/**
 * 실시간 알림 초기화 컴포넌트
 * - 로그인 시 SSE 연결을 맺고 실시간 알림을 수신함
 * - 알림 수신 시 전역 Toast 컴포넌트를 통해 알림 노출
 */
export function NotificationInitializer() {
  const { isAuthenticated } = useAuthStore();

  useEffect(() => {
    // 로그인 상태가 아닐 때는 구독하지 않음
    if (!isAuthenticated) return;

    // SSE 연결 (BFF의 프록시 엔드포인트 호출)
    // 네이티브 EventSource는 HttpOnly 쿠키를 자동으로 포함하여 전송함
    const eventSource = new EventSource('/api/notifications/subscribe');

    eventSource.onopen = () => {
      console.log('[SSE] 알림 서비스 연결 성공');
    };

    // 'notification' 이벤트 수신 (백엔드에서 지정한 이벤트 이름)
    eventSource.addEventListener('notification', (event) => {
      try {
        const data: NotificationResponse = JSON.parse(event.data);
        console.log('[SSE] 새 알림 수신:', data);

        // 알림 메시지를 Toast로 노출
        // 시착 완료("TRYON_COMPLETE") 등 모든 알림에 대해 Toast를 띄움
        toast.success(data.message);
      } catch (err) {
        console.error('[SSE] 알림 데이터 파싱 에러:', err);
      }
    });

    // 연결 에러 발생 시 처리 (브라우저가 자동으로 재연결을 시도함)
    eventSource.onerror = (err) => {
      console.error('[SSE] 알림 서비스 연결 에러:', err);
    };

    return () => {
      console.log('[SSE] 알림 서비스 연결 해제');
      eventSource.close();
    };
  }, [isAuthenticated]);

  return null; // UI 없이 로직만 수행
}
