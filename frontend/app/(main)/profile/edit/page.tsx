import Button from './_components/Button';
import styles from './page.module.css';

export const metadata = {
  title: '프로필 편집 — SIMUL',
  description: '프로필 정보를 수정하세요.',
};

export default function ProfileEditPage() {
  return (
    <div className={styles.editPage}>
      <h1>프로필 편집</h1>

      <div className={styles.avatarSection}>
        <div className={styles.avatar}>🧑</div>
        <button className={styles.changeAvatarBtn}>프로필 사진 변경</button>
      </div>

      <div className={styles.formGroup}>
        <label htmlFor="nickname">닉네임</label>
        <input
          id="nickname"
          type="text"
          className={styles.input}
          defaultValue="지수"
          placeholder="닉네임을 입력하세요"
        />
      </div>

      <div className={styles.formGroup}>
        <label htmlFor="bio">한줄 소개</label>
        <input
          id="bio"
          type="text"
          className={styles.input}
          defaultValue="패션을 사랑하는 SIMUL 유저 ✨"
          placeholder="자기소개를 입력하세요"
        />
      </div>

      <div className={styles.submitRow}>
        <Button variant="secondary" size="lg" fullWidth>취소</Button>
        <Button variant="primary" size="lg" fullWidth>저장</Button>
      </div>
    </div>
  );
}
