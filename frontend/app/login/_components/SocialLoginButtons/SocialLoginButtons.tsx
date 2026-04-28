'use client';

import styles from './SocialLoginButtons.module.css';

export default function SocialLoginButtons() {
  return (
    <>
      <div className={styles.socialButtons}>
        <button className={`${styles.socialBtn} ${styles.kakao}`}>
          <span className={styles.socialIcon}>💬</span>
          카카오로 시작하기
        </button>
        <button className={`${styles.socialBtn} ${styles.naver}`}>
          <span className={styles.socialIcon}>N</span>
          네이버로 시작하기
        </button>
        <button className={`${styles.socialBtn} ${styles.google}`}>
          <span className={styles.socialIcon}>G</span>
          Google로 시작하기
        </button>
      </div>

      <div className={styles.divider}>또는</div>

      <p className={styles.emailLink}>
        이메일로 로그인하려면 <a href="#">여기를 클릭</a>하세요
      </p>

      <p className={styles.privacyNote}>
        계속 진행하면 SIMUL의{' '}
        <a href="#">이용약관</a> 및{' '}
        <a href="#">개인정보처리방침</a>에 동의하게 됩니다.
      </p>
    </>
  );
}
