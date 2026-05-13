'use client';

import Link from 'next/link';
import { useEffect } from 'react';
import styles from './Header.module.css';
import { useNotificationStore } from '@/lib/stores/useNotificationStore';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import { notificationAPI } from '@/lib/api/notificationAPI';

export default function Header() {
  const { unreadCount, setUnreadCount } = useNotificationStore();
  const { isAuthenticated } = useAuthStore();

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
  return (
    <header className={styles.header}>
      <div className={styles.headerInner}>
        <Link href="/" className={styles.logo}>
          <img src="/logo.png" alt="SIMUL" className={styles.logoImage} />
        </Link>

        <div className={styles.rightIcons}>
          <button className={styles.iconBtn} aria-label="검색">
            <img src="/icons/magnifyingglass.png" alt="검색 아이콘" className={styles.iconImage} />
          </button>
          <button className={styles.iconBtn} aria-label="알림">
            <div className={styles.iconWrapper}>
              <img src="/icons/ring.png" alt="알림" className={styles.iconImage} />
              {unreadCount > 0 && (
                <span className={styles.badge}>{unreadCount > 99 ? '99+' : unreadCount}</span>
              )}
            </div>
          </button>
        </div>
      </div>
    </header>
  );
}
