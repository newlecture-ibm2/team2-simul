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
      await fetch('/api/auth/logout', { method: 'POST' });
      logout();
      router.push('/login');
    } catch (error) {
      console.error('로그아웃 실패:', error);
      logout();
      router.push('/login');
    }
  };

  return (
    <button className={styles.logoutBtn} onClick={handleLogout}>
      로그아웃
    </button>
  );
}
