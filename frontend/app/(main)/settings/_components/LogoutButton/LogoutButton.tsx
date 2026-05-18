'use client';

import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import styles from './LogoutButton.module.css';

/**
 * 로그아웃 버튼 컴포넌트
 */
export default function LogoutButton() {
  const router = useRouter();
  const logout = useAuthStore((state) => state.logout);

  const handleLogout = async () => {
    try {
      await fetch('/api/auth/logout', { 
        method: 'POST',
        credentials: 'include', // 배포 환경에서 세션 쿠키 전송 보장
      });
      logout();
      router.replace('/login');
    } catch (error) {
      console.error('로그아웃 실패:', error);
      // 서버 호출 실패해도 클라이언트 측 세션은 정리
      logout();
      router.replace('/login');
    }
  };

  return (
    <button className={styles.logoutBtn} onClick={handleLogout}>
      로그아웃
    </button>
  );
}
