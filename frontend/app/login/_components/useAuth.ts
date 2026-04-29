'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

export function useAuth() {
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const login = async (provider: 'kakao' | 'naver' | 'google') => {
    setIsLoading(true);
    
    // [인프라 미완성 상태의 Mock 로직]
    // 1. 실제 API 호출 대신 1.5초 지연을 줍니다 (서버 통신 흉내)
    console.log(`${provider} 로그인을 시도합니다...`);
    
    await new Promise((resolve) => setTimeout(resolve, 1500));
    
    // 2. 성공 시 메인 피드로 이동 (실제로는 여기서 JWT 토큰을 저장하게 됩니다)
    console.log(`${provider} 로그인 성공! 메인 페이지로 이동합니다.`);
    setIsLoading(false);
    router.push('/');
  };

  return { login, isLoading };
}
