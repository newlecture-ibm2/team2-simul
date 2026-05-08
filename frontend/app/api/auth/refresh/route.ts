import { NextRequest, NextResponse } from 'next/server';
import { getIronSession } from 'iron-session';
import { sessionOptions, SessionData } from '@/lib/session';

/**
 * 토큰 갱신 BFF 라우트
 * 
 * 1. 세션에서 refreshToken을 꺼냄
 * 2. 백엔드 /auth/refresh 호출
 * 3. 새 토큰으로 세션 업데이트
 */
export async function POST(request: NextRequest) {
  try {
    const response = NextResponse.json({ success: true });
    const session = await getIronSession<SessionData>(request, response, sessionOptions);

    const refreshToken = session.user?.refreshToken;

    if (!refreshToken) {
      return NextResponse.json(
        { message: '리프레시 토큰이 없습니다.' },
        { status: 401 }
      );
    }

    const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';
    const backendResponse = await fetch(`${backendUrl}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    });

    if (!backendResponse.ok) {
      // 리프레시 토큰도 만료된 경우 세션 파괴
      session.destroy();
      return NextResponse.json(
        { message: '세션이 만료되었습니다. 다시 로그인해주세요.' },
        { status: 401 }
      );
    }

    const data = await backendResponse.json();
    const { accessToken, refreshToken: newRefreshToken } = data;

    // 세션 정보 업데이트
    if (session.user) {
      session.user.token = accessToken;
      session.user.refreshToken = newRefreshToken;
      await session.save();
    }

    return response;

  } catch (error) {
    console.error('BFF Token Refresh Error:', error);
    return NextResponse.json(
      { message: '내부 서버 오류가 발생했습니다.' },
      { status: 500 }
    );
  }
}
