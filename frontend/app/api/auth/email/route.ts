import { NextRequest, NextResponse } from 'next/server';
import { getIronSession } from 'iron-session';
import { sessionOptions, SessionData } from '@/lib/session';

/**
 * 이메일 인증 BFF 라우트
 *
 * 1. 브라우저에서 이메일/비밀번호를 받음
 * 2. 백엔드(Spring Boot)에 전달하여 JWT 발급
 * 3. 발급받은 토큰을 iron-session에 암호화 저장
 */
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { email, password, name, nickname, gender, type } = body;
    const isSignup = type === 'signup';

    const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';
    const endpoint = isSignup ? '/auth/signup' : '/auth/login/email';
    
    const backendResponse = await fetch(`${backendUrl}${endpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(isSignup 
        ? { email, password, name, nickname, gender }
        : { email, password }
      ),
    });

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json();
      return NextResponse.json(
        { message: errorData.detail || errorData.message || '인증 실패' },
        { status: backendResponse.status }
      );
    }

    const data = await backendResponse.json();
    const { accessToken, refreshToken, isNewUser } = data;

    // iron-session에 토큰을 암호화하여 저장
    const response = NextResponse.json({ 
      success: true,
      isNewUser,
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
    console.error('BFF Email Auth Error:', error);
    return NextResponse.json(
      { message: '내부 서버 오류가 발생했습니다.' },
      { status: 500 }
    );
  }
}
