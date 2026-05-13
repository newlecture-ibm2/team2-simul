import styles from './page.module.css';
import PasswordChangeForm from './_components/PasswordChangeForm';
import Link from 'next/link';

export const metadata = {
  title: '비밀번호 변경 — SIMUL',
  description: '계정 비밀번호를 안전하게 변경하세요.',
};

export default function PasswordChangePage() {
  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <Link href="/settings" className={styles.backBtn}>
          ←
        </Link>
        <h1 className={styles.title}>비밀번호 변경</h1>
        <div style={{ width: '24px' }} /> {/* Spacer to center title */}
      </header>

      <div className={styles.content}>
        <p className={styles.description}>
          계정 보안을 위해 정기적으로 비밀번호를 변경하는 것이 좋습니다.
        </p>
        <PasswordChangeForm />
      </div>
    </div>
  );
}
