import { NextRequest, NextResponse } from 'next/server';

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
    const { accessToken, refreshToken } = data;

    const response = NextResponse.json({ 
      success: true,
      user: data.user 
    });

    response.cookies.set('accessToken', accessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      path: '/',
      maxAge: 3600, 
    });

    response.cookies.set('refreshToken', refreshToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      path: '/',
      maxAge: 60 * 60 * 24 * 14,
    });

    return response;

  } catch (error) {
    console.error('BFF Email Auth Error:', error);
    return NextResponse.json(
      { message: '내부 서버 오류가 발생했습니다.' },
      { status: 500 }
    );
  }
}
