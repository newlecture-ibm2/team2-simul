import styles from './page.module.css';
import SocialLoginButtons from './_components/SocialLoginButtons';

export const metadata = {
  title: '로그인 — SIMUL',
  description: 'SIMUL에 로그인하여 AI 가상시착을 시작하세요.',
};

export default function LoginPage() {
  return (
    <div className={styles.loginPage}>
      <div className={styles.loginCard}>
        <div className={styles.brandSection}>
          <div className={styles.logoMark}>S</div>
          <h1 className={styles.brandName}>SIMUL</h1>
          <p className={styles.brandTagline}>
            AI 가상시착으로 나만의 스타일을 발견하세요
          </p>
        </div>
        <SocialLoginButtons />
      </div>
    </div>
  );
}
