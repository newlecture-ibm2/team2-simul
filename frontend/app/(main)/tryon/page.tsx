import Link from 'next/link';
import Button from './_components/Button';
import styles from './page.module.css';

export const metadata = {
  title: '가상시착 — SIMUL',
  description: 'AI 가상시착을 시작하세요. 내 사진에 옷을 입혀보세요.',
};

export default function TryonHomePage() {
  return (
    <div className={styles.tryonHome}>
      <div className={styles.headerRow}>
        <h1 className={styles.title}>가상시착</h1>
        <div className={styles.creditBadge}>
          크레딧: 3 / 5
        </div>
      </div>

      <div className={styles.heroSection}>
        <div className={styles.heroCards}>
          <div className={`${styles.heroCard} ${styles.cardLeft}`}>
            <img src="/dummy.jpg" alt="Model 1" />
          </div>
          <div className={`${styles.heroCard} ${styles.cardCenter}`}>
            <img src="/recent.jpg" alt="Model 2" />
          </div>
          <div className={`${styles.heroCard} ${styles.cardRight}`}>
            <img src="/temp.jpg" alt="Model 3" />
          </div>
        </div>

        <div className={styles.heroTextContent}>
          <h2 className={styles.heroTitle}>
            AI로 옷을 미리 입어보세요
          </h2>
          <p className={styles.heroDesc}>
            내 사진에 원하는 옷을 합성해<br />
            가상으로 시착해볼 수 있어요.<br />
            구매 전 미리 확인하고 현명한 쇼핑을 시작하세요.
          </p>
          <Link href="/tryon/studio">
            <Button variant="primary" size="lg">시착 시작하기</Button>
          </Link>
        </div>
      </div>

      <div className={styles.recentSection}>
        <h2>최근 시착 결과</h2>
        <div className={styles.recentScroll}>
          {Array.from({ length: 6 }, (_, i) => (
            <div key={i} className={styles.recentItem}>
              <img src="/recent.jpg" alt={`Recent Try-on ${i + 1}`} className={styles.recentImage} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
