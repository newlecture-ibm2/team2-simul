'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './PasswordChangeForm.module.css';
import { changePassword } from '@/lib/api/userAPI';
import { toast } from '@/lib/utils/toast';

export default function PasswordChangeForm() {
  const router = useRouter();
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (newPassword !== confirmPassword) {
      toast.error('새 비밀번호가 일치하지 않습니다.');
      return;
    }

    if (newPassword.length < 8) {
      toast.error('비밀번호는 8자 이상이어야 합니다.');
      return;
    }

    setIsLoading(true);
    try {
      await changePassword({ oldPassword, newPassword });
      toast.success('비밀번호가 성공적으로 변경되었습니다.');
      router.push('/settings');
    } catch (error: unknown) {
      console.error('Password change failed:', error);
      const axiosError = error as { response?: { data?: { message?: string } } };
      const message = axiosError.response?.data?.message || '비밀번호 변경에 실패했습니다.';
      toast.error(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <div className={styles.inputGroup}>
        <label htmlFor="oldPassword">현재 비밀번호</label>
        <input
          id="oldPassword"
          type="password"
          value={oldPassword}
          onChange={(e) => setOldPassword(e.target.value)}
          placeholder="현재 비밀번호를 입력하세요"
          required
        />
      </div>

      <div className={styles.inputGroup}>
        <label htmlFor="newPassword">새 비밀번호</label>
        <input
          id="newPassword"
          type="password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          placeholder="새 비밀번호 (8자 이상)"
          required
        />
      </div>

      <div className={styles.inputGroup}>
        <label htmlFor="confirmPassword">새 비밀번호 확인</label>
        <input
          id="confirmPassword"
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          placeholder="새 비밀번호를 한번 더 입력하세요"
          required
        />
      </div>

      <button type="submit" className={styles.submitBtn} disabled={isLoading}>
        {isLoading ? '변경 중...' : '비밀번호 변경'}
      </button>
    </form>
  );
}
