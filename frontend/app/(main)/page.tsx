'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import styles from './page.module.css';
import SwipeDeck from './_components/SwipeDeck/SwipeDeck';

export default function HomePage() {
  const router = useRouter();

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const hasSeenSplash = sessionStorage.getItem('simul_splash_seen');
      if (!hasSeenSplash) {
        sessionStorage.setItem('simul_splash_seen', 'true');
        router.replace('/splash');
      }
    }
  }, [router]);

  return (
    <div className={styles.homePage}>
      <SwipeDeck />
    </div>
  );
}
