import { NextRequest, NextResponse } from 'next/server';

/**
 * 로그아웃 BFF 라우트
 * HttpOnly 쿠키(accessToken, refreshToken)를 삭제 처리합니다.
 */
export async function POST(request: NextRequest) {
  const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';
  
  // 백엔드 로그아웃 API 호출 (성공 여부 상관없이 쿠키는 삭제)
  try {
    await fetch(`${backendUrl}/auth/logout`, {
      method: 'DELETE',
    });
  } catch (error) {
    console.error('Backend logout call failed:', error);
  }

  const response = NextResponse.json({ success: true });

  // 쿠키 만료 처리
  response.cookies.set('accessToken', '', {
    httpOnly: true,
    path: '/',
    maxAge: 0,
  });

  response.cookies.set('refreshToken', '', {
    httpOnly: true,
    path: '/',
    maxAge: 0,
  });

  return response;
}
