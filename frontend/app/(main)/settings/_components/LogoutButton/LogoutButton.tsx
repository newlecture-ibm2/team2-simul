'use client';

import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import styles from '../../page.module.css';

/**
 * 로그아웃 버튼 컴포넌트
 *
 * 클릭 시:
 * 1. BFF 로그아웃 API 호출 → iron-session 파괴 + 백엔드 Redis 토큰 삭제
 * 2. Zustand 클라이언트 상태 초기화
 * 3. 로그인 페이지로 이동
 */
export default function LogoutButton() {
  const router = useRouter();
  const logout = useAuthStore((state) => state.logout);

  const handleLogout = async () => {
    try {
      // 1. BFF 로그아웃 API 호출 (서버 세션 파괴 + Redis 토큰 삭제)
      await fetch('/api/auth/logout', { method: 'POST' });

      // 2. Zustand 클라이언트 상태 초기화
      logout();

      // 3. 로그인 페이지로 이동
      router.push('/login');
    } catch (error) {
      console.error('로그아웃 실패:', error);
      // 실패해도 클라이언트 상태는 정리하고 로그인 페이지로 이동
      logout();
      router.push('/login');
    }
  };

  return (
    <button className={styles.dangerBtn} onClick={handleLogout}>
      로그아웃
    </button>
  );
}
