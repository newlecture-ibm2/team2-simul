'use client';

import { useAuth } from '../useAuth';
import styles from './SocialLoginButtons.module.css';

export default function SocialLoginButtons() {
  const { login, isLoading } = useAuth();

  return (
    <>
      <div className={styles.socialButtons}>
        <button 
          className={`${styles.socialBtn} ${styles.google}`}
          onClick={() => login('google')}
          disabled={isLoading}
        >
          <span className={styles.socialIcon}>G</span>
          {isLoading ? '연결 중...' : 'Google로 시작하기'}
        </button>
        <button 
          className={`${styles.socialBtn} ${styles.kakao}`}
          onClick={() => login('kakao')}
          disabled={isLoading}
        >
          <span className={styles.socialIcon}>💬</span>
          {isLoading ? '연결 중...' : '카카오로 시작하기'}
        </button>
        <button 
          className={`${styles.socialBtn} ${styles.naver}`}
          onClick={() => login('naver')}
          disabled={isLoading}
        >
          <span className={styles.socialIcon}>N</span>
          {isLoading ? '연결 중...' : '네이버로 시작하기'}
        </button>
      </div>

      <p className={styles.privacyNote}>
        계속 진행하면 SIMUL의{' '}
        <a href="#">이용약관</a> 및{' '}
        <a href="#">개인정보처리방침</a>에 동의하게 됩니다.
      </p>
    </>
  );
}
