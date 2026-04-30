'use client';

import { useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';

function CallbackHandler() {
  const searchParams = useSearchParams();
  const router = useRouter();

  useEffect(() => {
    const code = searchParams.get('code');
    const state = searchParams.get('state');

    if (code) {
      console.log('네이버 인가 코드를 수신했습니다. BFF로 로그인을 요청합니다...');
      
      // 내부 BFF API 호출 (Next.js API Route)
      fetch('/api/auth/social', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          provider: 'naver',
          code: code,
          redirectUri: 'http://localhost:3000/auth/callback/naver'
        }),
      })
      .then(async (res) => {
        if (!res.ok) {
          const errorData = await res.json();
          throw new Error(errorData.message || '로그인 요청 실패');
        }
        return res.json();
      })
      .then((data) => {
        console.log('로그인 성공!', data);
        // TODO: JWT 토큰 처리 로직 (현재는 성공 로그만 출력)
        // 성공 시 홈 피드로 이동
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
      <p style={{ fontSize: '18px', fontWeight: 'bold' }}>네이버 로그인 처리 중...</p>
      <p style={{ color: '#666' }}>잠시만 기다려주세요.</p>
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
