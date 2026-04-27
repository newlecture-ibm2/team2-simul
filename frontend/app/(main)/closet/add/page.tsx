import Button from './_components/Button';
import styles from './page.module.css';

export const metadata = {
  title: '아이템 추가 — SIMUL',
  description: '새 아이템을 내 옷장에 추가하세요.',
};

export default function ClosetAddPage() {
  return (
    <div className={styles.addItem}>
      <h1>아이템 추가</h1>

      <div className={styles.uploadArea}>
        <span className={styles.uploadIcon}>📷</span>
        <span className={styles.uploadText}>옷 이미지를 업로드하세요</span>
      </div>

      <div className={styles.formGroup}>
        <label htmlFor="category">카테고리</label>
        <select id="category" className={styles.select}>
          <option value="">카테고리 선택</option>
          <option value="top">상의</option>
          <option value="bottom">하의</option>
          <option value="outer">아우터</option>
          <option value="shoes">신발</option>
          <option value="accessory">액세서리</option>
        </select>
      </div>

      <div className={styles.formGroup}>
        <label htmlFor="memo">메모 (선택)</label>
        <textarea
          id="memo"
          className={styles.memoInput}
          placeholder="아이템에 대한 메모를 남겨보세요..."
          maxLength={100}
        />
        <div className={styles.memoCounter}>0 / 100</div>
      </div>

      <div className={styles.submitRow}>
        <Button variant="secondary" size="lg" fullWidth>취소</Button>
        <Button variant="primary" size="lg" fullWidth>저장</Button>
      </div>
    </div>
  );
}
