import { NextRequest, NextResponse } from 'next/server';

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
    const { accessToken, refreshToken } = data;

    // 3. 응답 생성 및 JWT를 httpOnly 쿠키에 저장
    const response = NextResponse.json({ 
      success: true,
      user: data.user 
    });

    // Access Token 쿠키 설정
    response.cookies.set('accessToken', accessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      path: '/',
      maxAge: 3600, 
    });

    // Refresh Token 쿠키 설정
    response.cookies.set('refreshToken', refreshToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      path: '/',
      maxAge: 60 * 60 * 24 * 14,
    });

    return response;

  } catch (error) {
    console.error('BFF Auth Error:', error);
    return NextResponse.json(
      { message: '내부 서버 오류가 발생했습니다.' },
      { status: 500 }
    );
  }
}
