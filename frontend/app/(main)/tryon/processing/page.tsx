import Link from 'next/link';
import Button from '@/components/Button';
import styles from './page.module.css';

export const metadata = {
  title: '시착 생성 중 — SIMUL',
  description: 'AI가 가상시착 결과를 생성하고 있습니다.',
};

export default function ProcessingPage() {
  return (
    <div className={styles.processing}>
      <div className={styles.progressSection}>
        <div className={styles.progressBar}>
          <div className={styles.progressFill} />
        </div>
        <span className={styles.progressText}>결과 생성중... 잠시만 기다려 주세요.<br/>
        보통 10~30초 정도 소요됩니다.</span>
      </div>
      <div className={styles.feedPreview}>
        <div className={styles.frameText}>
          <h2>더 마음에 드는 피드를 골라보세요!</h2>
          <p>기다리는 동안 인기 피드를 만나보세요.<br/>선택결과를 반영해 당신의 취향을 추천해드립니다.</p>
        </div>
        <div className={`${styles.circle} ${styles.circle1}`}>
          <img src="/dummy.jpg" alt="feed 1" />
        </div>
        <div className={`${styles.circle} ${styles.circle2}`}>
          <img src="/recent.jpg" alt="feed 2" />
        </div>
        <div className={`${styles.circle} ${styles.circle3}`}>
          <img src="/temp.jpg" alt="feed 3" />
        </div>
      </div>
      <Link href="/tryon/result" style={{ width: '100%' }}>
        <Button variant="large-dark" fullWidth>결과 보기 (임시)</Button>
      </Link>
    </div>
  );
}
