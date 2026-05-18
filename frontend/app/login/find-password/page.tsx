import React from 'react';
import styles from './page.module.css';
import FindPasswordWizard from './_components/FindPasswordWizard/FindPasswordWizard';

export default function FindPasswordPage() {
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>비밀번호 찾기</h1>
      <p className={styles.subtitle}>
        가입하신 이메일을 통해 비밀번호를 재설정할 수 있습니다.
      </p>
      <FindPasswordWizard />
    </div>
  );
}
