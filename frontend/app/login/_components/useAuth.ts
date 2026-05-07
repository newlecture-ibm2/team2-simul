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
      const redirectUri = encodeURIComponent(`${window.location.origin}/auth/callback/naver`);
      const state = encodeURIComponent(Math.random().toString(36).substring(2));
      
      // 네이버 인증 페이지로 이동
      window.location.href = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
      return;
    }

    if (provider === 'kakao') {
      const clientId = process.env.NEXT_PUBLIC_KAKAO_CLIENT_ID;
      const redirectUri = encodeURIComponent(`${window.location.origin}/auth/callback/kakao`);
      
      // 카카오 인증 페이지로 이동
      window.location.href = `https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}`;
      return;
    }

    if (provider === 'google') {
      const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
      const redirectUri = encodeURIComponent(`${window.location.origin}/auth/callback/google`);
      
      // Google 인증 페이지로 이동
      window.location.href = `https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&scope=email%20profile`;
      return;
    }
  };

  return { login, isLoading };
}
