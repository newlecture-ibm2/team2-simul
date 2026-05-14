'use client';

import { useEffect, useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import axios from 'axios';
import styles from './page.module.css';

function VerifyContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('이메일 인증을 진행 중입니다...');

  useEffect(() => {
    const token = searchParams.get('token');

    if (!token) {
      setStatus('error');
      setMessage('유효하지 않은 인증 링크입니다.');
      return;
    }

    const verifyEmail = async () => {
      try {
        await axios.get(`/api/auth/verify-email?token=${token}`);
        setStatus('success');
        setMessage('이메일 인증이 완료되었습니다! 이제 로그인하여 서비스를 이용하실 수 있습니다.');
      } catch (error: any) {
        setStatus('error');
        const errorMsg = error.response?.data?.message || '인증에 실패했습니다. 만료된 링크일 수 있습니다.';
        setMessage(errorMsg);
      }
    };

    verifyEmail();
  }, [searchParams]);

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>
          {status === 'loading' && '인증 중...'}
          {status === 'success' && '인증 성공! 🎉'}
          {status === 'error' && '인증 실패 ❌'}
        </h1>
        <p className={styles.message}>{message}</p>
        
      </div>
    </div>
  );
}

export default function VerifyEmailPage() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <VerifyContent />
    </Suspense>
  );
}
