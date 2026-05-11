'use client';

import { useState, useEffect } from 'react';
import styles from './OfflineBanner.module.css';

export default function OfflineBanner() {
  const [isOffline, setIsOffline] = useState(false);

  useEffect(() => {
    // 초기 로드시 네트워크 상태 확인
    if (typeof window !== 'undefined') {
      setIsOffline(!window.navigator.onLine);
    }

    const handleOnline = () => setIsOffline(false);
    const handleOffline = () => setIsOffline(true);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  if (!isOffline) return null;

  return (
    <div className={styles.banner} role="alert">
      <p>인터넷 연결이 끊어졌습니다. 네트워크 상태를 확인해 주세요.</p>
    </div>
  );
}
