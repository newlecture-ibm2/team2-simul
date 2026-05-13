'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { SplashLogo } from './_components/SplashLogo/SplashLogo';
import styles from './page.module.css';

export default function SplashPage() {
  const router = useRouter();

  useEffect(() => {
    // 2.5초 후 메인 피드로 이동 (애니메이션 완료 시간 고려)
    const timer = setTimeout(() => {
      router.push('/');
    }, 2500);

    return () => clearTimeout(timer);
  }, [router]);

  return (
    <div className={styles.splashContainer}>
      <div className={styles.content}>
        <SplashLogo />
        <div className={styles.taglineSection}>
          <p className={styles.tagline}>AI 가상시착 패션 플랫폼</p>
        </div>
      </div>
      <div className={styles.footer}>
        <p className={styles.copyright}>© 2026 SIMUL. All rights reserved.</p>
      </div>
    </div>
  );
}
