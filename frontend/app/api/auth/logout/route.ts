import { NextRequest, NextResponse } from 'next/server';
import { getIronSession } from 'iron-session';
import { sessionOptions, SessionData } from '@/lib/session';

/**
 * 로그아웃 BFF 라우트
 *
 * 1. iron-session에서 refreshToken을 꺼냄
 * 2. 백엔드 /auth/logout에 refreshToken을 보내서 Redis에서 삭제
 * 3. iron-session 세션을 파괴 (암호화 쿠키 삭제)
 */
export async function POST(request: NextRequest) {
  const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';

  // 1. iron-session에서 현재 세션 정보 가져오기
  const response = NextResponse.json({ success: true });
  const session = await getIronSession<SessionData>(request, response, sessionOptions);

  // 2. 세션에 refreshToken이 있으면 백엔드에 보내서 Redis에서 삭제 (Access Token도 함께 전달하여 블랙리스트 처리)
  const refreshToken = session.user?.refreshToken;
  const accessToken = session.user?.token;
  
  if (refreshToken || accessToken) {
    try {
      const headers: Record<string, string> = { 'Content-Type': 'application/json' };
      if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
      }
      
      await fetch(`${backendUrl}/auth/logout`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ refreshToken }),
      });
    } catch (error) {
      console.error('[Logout] Backend logout call failed:', error);
    }
  }

  // 3. iron-session 세션 파괴 (브라우저의 암호화 쿠키도 자동 삭제)
  session.destroy();

  return response;
}
