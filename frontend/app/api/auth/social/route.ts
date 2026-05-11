import { NextRequest, NextResponse } from 'next/server';
import { getIronSession } from 'iron-session';
import { sessionOptions, SessionData } from '@/lib/session';

/**
 * 소셜 로그인 BFF 라우트
 *
 * 1. 브라우저에서 provider, code, redirectUri를 받음
 * 2. 백엔드(Spring Boot)에 전달하여 JWT 발급
 * 3. 발급받은 토큰을 iron-session에 암호화 저장
 */
export async function POST(request: NextRequest) {
  try {
    // 1. 클라이언트(브라우저)로부터 전달받은 데이터 추출
    const body = await request.json();
    const { provider, code, redirectUri } = body;

    // 2. 실제 백엔드(Spring Boot) 서버로 로그인 요청 전달
    // 서버 간 통신이므로 브라우저의 CORS 제한을 받지 않습니다.
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';
    const backendResponse = await fetch(`${backendUrl}/auth/social`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ provider, code, redirectUri }),
    });

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json();
      return NextResponse.json(
        { message: errorData.message || '백엔드 인증 실패' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    const { accessToken, refreshToken, isNewUser } = data;

    // 새로 발급받은 토큰으로 유저 정보 조회
    let user = null;
    try {
      const userResponse = await fetch(`${backendUrl}/users/me`, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });
      if (userResponse.ok) {
        user = await userResponse.json();
      }
    } catch (e) {
      console.error('BFF Fetch User Error:', e);
    }

    // 3. iron-session에 토큰을 암호화하여 저장
    const response = NextResponse.json({ 
      success: true,
      isNewUser,
      user,
    });

    const session = await getIronSession<SessionData>(request, response, sessionOptions);
    session.user = {
      id: '', // 토큰에서 추출 가능하지만, 당장은 빈 값으로 설정
      role: 'USER',
      token: accessToken,
      refreshToken: refreshToken,
    };
    await session.save();

    return response;

  } catch (error) {
    console.error('BFF Social Auth Error:', error);
    return NextResponse.json(
      { message: '내부 서버 오류가 발생했습니다.' },
      { status: 500 }
    );
  }
}
