'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import styles from './FindPasswordWizard.module.css';
import { requestResetCode, verifyResetCode, resetPasswordWithCode } from '@/lib/api/authAPI';
import { toast } from '@/lib/utils/toast';

type Step = 'EMAIL' | 'VERIFY' | 'RESET';

export default function FindPasswordWizard() {
  const router = useRouter();
  const [step, setStep] = useState<Step>('EMAIL');
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  // 15분 타이머 상태
  const [timeLeft, setTimeLeft] = useState(900);

  useEffect(() => {
    if (step !== 'VERIFY') return;
    
    if (timeLeft <= 0) {
      toast.error('인증 시간이 만료되었습니다. 처음부터 다시 시도해주세요.');
      setStep('EMAIL');
      setCode('');
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft(prev => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [step, timeLeft]);

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  const handleRequestCode = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) return;

    try {
      setIsLoading(true);
      await requestResetCode(email);
      toast.success('인증번호가 이메일로 발송되었습니다.');
      setTimeLeft(900); // 15분 초기화
      setStep('VERIFY');
    } catch (error) {
      // toast is handled globally by Axios interceptor, but we can log it here
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyCode = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!code) return;

    try {
      setIsLoading(true);
      await verifyResetCode(email, code);
      toast.success('이메일 인증이 완료되었습니다. 새 비밀번호를 입력해주세요.');
      setStep('RESET');
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPassword || newPassword !== confirmPassword) {
      toast.error('비밀번호가 일치하지 않습니다.');
      return;
    }

    try {
      setIsLoading(true);
      await resetPasswordWithCode(email, code, newPassword);
      toast.success('비밀번호가 성공적으로 재설정되었습니다. 로그인해주세요.');
      router.push('/login');
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.wizardContainer}>
      {step === 'EMAIL' && (
        <form onSubmit={handleRequestCode}>
          <div className={styles.inputGroup}>
            <label className={styles.label}>이메일 주소</label>
            <input
              type="email"
              className={styles.input}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="가입한 이메일을 입력해주세요"
              required
            />
          </div>
          <button type="submit" className={styles.submitBtn} disabled={isLoading || !email}>
            {isLoading ? '발송 중...' : '인증번호 받기'}
          </button>
        </form>
      )}

      {step === 'VERIFY' && (
        <form onSubmit={handleVerifyCode}>
          <div className={styles.inputGroup}>
            <label className={styles.label}>인증번호 6자리</label>
            <input
              type="text"
              className={styles.input}
              value={code}
              onChange={(e) => setCode(e.target.value)}
              placeholder="인증번호를 입력해주세요"
              maxLength={6}
              required
            />
            <span className={styles.timer}>{formatTime(timeLeft)}</span>
          </div>
          <button type="submit" className={styles.submitBtn} disabled={isLoading || code.length < 6}>
            {isLoading ? '확인 중...' : '인증하기'}
          </button>
        </form>
      )}

      {step === 'RESET' && (
        <form onSubmit={handleResetPassword}>
          <div className={styles.inputGroup}>
            <label className={styles.label}>새 비밀번호</label>
            <input
              type="password"
              className={styles.input}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="새로운 비밀번호를 입력해주세요"
              required
            />
          </div>
          <div className={styles.inputGroup}>
            <label className={styles.label}>비밀번호 확인</label>
            <input
              type="password"
              className={styles.input}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="비밀번호를 다시 입력해주세요"
              required
            />
          </div>
          <button type="submit" className={styles.submitBtn} disabled={isLoading || !newPassword || !confirmPassword}>
            {isLoading ? '변경 중...' : '비밀번호 변경하기'}
          </button>
        </form>
      )}
    </div>
  );
}
