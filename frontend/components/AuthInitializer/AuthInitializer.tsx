'use client';

import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getCurrentUser } from '@/lib/api/authAPI';
import { useAuthStore } from '@/lib/stores/useAuthStore';

/**
 * AuthInitializer 컴포넌트
 * - 앱 로드 시 서버로부터 현재 로그인된 유저 정보를 가져와서 Zustand 스토어에 동기화합니다.
 */
export default function AuthInitializer() {
  const { setUser, isAuthenticated } = useAuthStore();

  // 1. 현재 로그인 유저 정보 조회
  const { data: user, isSuccess, isError } = useQuery({
    queryKey: ['me'],
    queryFn: getCurrentUser,
    // 1) 기본적으로 재시도 안 함
    retry: false,
    // 2) 5분간 데이터 유지
    staleTime: 1000 * 60 * 5,
    // 3) 에러 발생 시(401, 403 등) 해당 쿼리를 자동으로 다시 실행하지 않도록 설정
    gcTime: 0,
  });

  // 2. 조회 성공 시 Zustand 스토어 업데이트
  useEffect(() => {
    if (isSuccess && user) {
      setUser(user);
    }
  }, [isSuccess, user, setUser]);

  // 3. 조회 실패(인증 만료 등) 시 스토어 초기화
  useEffect(() => {
    if (isError) {
      setUser(null);
    }
  }, [isError, setUser]);

  return null; // UI는 렌더링하지 않음
}
