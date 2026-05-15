'use client';

import React, { useEffect, useRef, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuth } from '@/app/login/_components/useAuth';
import ConfirmModal from '@/components/ConfirmModal/ConfirmModal';

function CallbackHandler() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { 
    socialCallback, 
    handleRestore, 
    isRestoreModalOpen, 
    setIsRestoreModalOpen,
    restoreMessage,
    authMode
  } = useAuth();

  const hasCalled = useRef(false);

  useEffect(() => {
    const code = searchParams.get('code');
    const state = searchParams.get('state');

    if (code && !hasCalled.current) {
      hasCalled.current = true;
      socialCallback('naver', code, `${window.location.origin}/auth/callback/naver`);
    }
  }, [searchParams, socialCallback]);

  return (
    <div style={{ 
      display: 'flex', 
      flexDirection: 'column',
      alignItems: 'center', 
      justifyContent: 'center', 
      height: '100vh',
      gap: '20px'
    }}>
      <div className="loading-spinner"></div>
      <p style={{ fontSize: '18px', fontWeight: 'bold' }}>네이버 로그인 처리 중...</p>
      <p style={{ color: '#666' }}>잠시만 기다려주세요.</p>

      <ConfirmModal
        isOpen={isRestoreModalOpen}
        title={authMode === 'login' ? '계정 복구 안내' : '가입 안내'}
        description={restoreMessage}
        confirmText={authMode === 'login' ? '복구하기' : '확인'}
        cancelText={authMode === 'login' ? '아니오' : ''}
        onConfirm={authMode === 'login' ? handleRestore : () => setIsRestoreModalOpen(false)}
        onCancel={() => {
          setIsRestoreModalOpen(false);
          router.push('/');
        }}
      />
    </div>
  );
}

export default function NaverCallbackPage() {
  return (
    <Suspense fallback={<div>로딩 중...</div>}>
      <CallbackHandler />
    </Suspense>
  );
}
