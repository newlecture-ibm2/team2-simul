'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

export function useAuth() {
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const login = async (provider: 'kakao' | 'naver' | 'google') => {
    setIsLoading(true);
    
    if (provider === 'naver') {
      const clientId = process.env.NEXT_PUBLIC_NAVER_CLIENT_ID;
      const redirectUri = encodeURIComponent('http://localhost:3000/auth/callback/naver');
      const state = encodeURIComponent(Math.random().toString(36).substring(2));
      
      // 네이버 인증 페이지로 이동
      window.location.href = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
      return;
    }

    // [Mock logic for others]
    console.log(`${provider} 로그인을 시도합니다...`);
    await new Promise((resolve) => setTimeout(resolve, 1500));
    setIsLoading(false);
    router.push('/');
  };

  return { login, isLoading };
}
