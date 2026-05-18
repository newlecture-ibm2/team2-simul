'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import styles from './Header.module.css';
import { useNotificationStore } from '@/lib/stores/useNotificationStore';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import { notificationAPI } from '@/lib/api/notificationAPI';
import { NotificationPanel } from '@/components/NotificationPanel';

export default function Header() {
  const pathname = usePathname();
  const { unreadCount, setUnreadCount } = useNotificationStore();
  const { isAuthenticated, user } = useAuthStore();
  const [isNotificationOpen, setIsNotificationOpen] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      // 초기 미읽음 알림 수 로드
      notificationAPI.getUnreadCount()
        .then(res => {
          setUnreadCount(res.unread_count);
        })
        .catch(err => console.error('미읽음 알림 수 로드 실패:', err));
    }
  }, [isAuthenticated, setUnreadCount]);

  /** 알림 버튼 클릭 핸들러 */
  const handleNotificationToggle = () => {
    if (!isAuthenticated) return; // 비로그인 시 무시
    setIsNotificationOpen(prev => !prev);
  };

  return (
    <header className={styles.header}>
      <div className={styles.headerInner}>
        <Link href="/" className={styles.logo}>
          <img src="/logo.png" alt="SIMUL" className={styles.logoImage} />
        </Link>

        <div className={styles.rightIcons}>
          {user?.role === 'ADMIN' && (
            pathname.startsWith('/admin') ? (
              <Link href="/" className={styles.adminBtn}>
                홈으로
              </Link>
            ) : (
              <Link href="/admin" className={styles.adminBtn}>
                Admin
              </Link>
            )
          )}
          <Link href="/search" className={styles.iconBtn} aria-label="검색">
            <img src="/icons/magnifyingglass.png" alt="검색 아이콘" className={styles.iconImage} />
          </Link>
          <Link href="/profile" className={styles.iconBtn} aria-label="마이 페이지">
            <img src="/icons/profile.png" alt="마이 페이지 아이콘" className={styles.iconImage} />
          </Link>
          <button
            className={styles.iconBtn}
            aria-label="알림"
            onClick={handleNotificationToggle}
          >
            <div className={styles.iconWrapper}>
              <img src="/icons/ring.png" alt="알림" className={styles.iconImage} />
              {unreadCount > 0 && (
                <span className={styles.badge}>{unreadCount > 99 ? '99+' : unreadCount}</span>
              )}
            </div>
          </button>
        </div>
      </div>

      {/* 알림 패널 */}
      <NotificationPanel
        isOpen={isNotificationOpen}
        onClose={() => setIsNotificationOpen(false)}
      />
    </header>
  );
}
