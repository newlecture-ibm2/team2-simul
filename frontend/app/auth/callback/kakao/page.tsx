'use client';

import { useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { socialLogin } from '@/lib/api/authAPI';

function CallbackHandler() {
  const searchParams = useSearchParams();
  const router = useRouter();

  useEffect(() => {
    const code = searchParams.get('code');

    if (code) {
      console.log('카카오 인가 코드를 수신했습니다. BFF로 로그인을 요청합니다...');

      const redirectUri = `${window.location.origin}/auth/callback/kakao`;
      socialLogin('kakao', code, redirectUri)
        .then((data) => {
          console.log('로그인 성공!', data);
          router.push('/');
        })
        .catch((err) => {
          console.error('로그인 중 오류 발생:', err);
          alert('로그인에 실패했습니다. 다시 시도해주세요.');
          router.push('/login');
        });
    }
  }, [searchParams, router]);

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
      <p style={{ fontSize: '18px', fontWeight: 'bold' }}>카카오 로그인 처리 중...</p>
      <p style={{ color: '#666' }}>잠시만 기다려주세요.</p>
    </div>
  );
}

export default function KakaoCallbackPage() {
  return (
    <Suspense fallback={<div>로딩 중...</div>}>
      <CallbackHandler />
    </Suspense>
  );
}
