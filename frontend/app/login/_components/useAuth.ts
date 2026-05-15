'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

import { emailLogin, emailSignup, restoreAccount } from '../../../lib/api/authAPI';
import { useAuthStore } from '../../../lib/stores/useAuthStore';
import { toast } from '../../../lib/utils/toast';

export function useAuth() {
  const [isLoading, setIsLoading] = useState(false);
  const [isRestoreModalOpen, setIsRestoreModalOpen] = useState(false);
  const [restoreInfo, setRestoreInfo] = useState<{ provider: string; providerId: string } | null>(null);
  const [restoreMessage, setRestoreMessage] = useState('');
  const [authMode, setAuthMode] = useState<'login' | 'signup'>('login');
  
  const router = useRouter();
  const setUser = useAuthStore((state) => state.setUser);

  const handleLoginSuccess = async (res: { accessToken: string; isNewUser?: boolean }) => {
    if (res.accessToken) {
      const { getCurrentUser } = await import('../../../lib/api/authAPI');
      const user = await getCurrentUser();
      setUser(user);
      router.push(res.isNewUser ? '/profile/edit' : '/');
      return true;
    }
    return false;
  };

  const handleError = (error: unknown, provider: string, providerId: string, mode: 'login' | 'signup' = 'login') => {
    const err = error as { code?: string; message?: string };
    if (err.code === 'ERR-006') {
      // 탈퇴 유예 기간인 경우 모달 띄우기
      // 메시지에 ID가 포함되어 있는지 확인 (소셜 로그인의 경우)
      const parts = (err.message || '').split('|ID:');
      const displayMessage = parts[0];
      const extractedId = parts[1] || providerId;

      setRestoreInfo({ provider, providerId: extractedId });
      setRestoreMessage(displayMessage || '최근 탈퇴한 계정입니다. 계정을 복구하시겠습니까?');
      setAuthMode(mode);
      setIsRestoreModalOpen(true);
      return;
    }

    if (err.code === 'ERR-007') {
      // 이메일 인증이 필요한 경우
      setRestoreMessage(err.message || '이메일 인증이 완료되지 않았습니다. 메일함을 확인해 주세요.');
      setAuthMode('signup'); // 가입 안내 모드로 활용
      setIsRestoreModalOpen(true);
      return;
    }

    alert(err.message || '요청에 실패했습니다.');
  };

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

  const socialCallback = async (provider: string, code: string, redirectUri: string) => {
    setIsLoading(true);
    try {
      const { socialLogin } = await import('../../../lib/api/authAPI');
      const res = await socialLogin(provider, code, redirectUri);
      await handleLoginSuccess(res);
    } catch (error) {
      // 소셜 로그인의 경우 providerId는 백엔드 에러 메시지에 포함되거나, 
      // 백엔드가 세션에 임시 저장할 수도 있으나, 여기서는 일단 provider만 넘김
      handleError(error, provider, '', 'login'); 
    } finally {
      setIsLoading(false);
    }
  };

  const loginWithEmail = async (data: Record<string, unknown>) => {
    setIsLoading(true);
    try {
      const res = await emailLogin(data);
      await handleLoginSuccess(res);
    } catch (error) {
      handleError(error, 'email', data.email as string, 'login');
    } finally {
      setIsLoading(false);
    }
  };

  const signupWithEmail = async (data: Record<string, unknown>) => {
    setIsLoading(true);
    try {
      const res = await emailSignup(data);
      if (!res.accessToken) {
        // 이메일 인증이 필요한 경우 (백엔드에서 성공 응답을 주되 토큰을 비워서 보냄)
        setRestoreMessage('회원가입이 완료되었습니다! 가입하신 이메일로 발송된 인증 링크를 클릭하여 계정을 활성화해 주세요.');
        setAuthMode('signup');
        setIsRestoreModalOpen(true);
        return;
      }
      await handleLoginSuccess(res);
    } catch (error) {
      handleError(error, 'email', data.email as string, 'signup');
    } finally {
      setIsLoading(false);
    }
  };

  const handleRestore = async () => {
    if (!restoreInfo) return;
    setIsLoading(true);
    try {
      const res = await restoreAccount(restoreInfo.provider, restoreInfo.providerId);
      if (await handleLoginSuccess(res)) {
        toast.success('계정이 성공적으로 복구되었습니다.');
        setIsRestoreModalOpen(false);
      }
    } catch (error) {
      const err = error as { message?: string };
      alert(err.message || '복구에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return { 
    login, 
    loginWithEmail, 
    signupWithEmail, 
    socialCallback,
    handleRestore,
    isLoading,
    isRestoreModalOpen,
    setIsRestoreModalOpen,
    restoreInfo,
    restoreMessage,
    authMode
  };
}
