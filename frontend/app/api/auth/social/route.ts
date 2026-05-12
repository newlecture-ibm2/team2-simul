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

    // JWT payload에서 userId와 role 추출 (세션 저장용)
    let userId = '';
    let userRole: 'USER' | 'ADMIN' = 'USER';
    try {
      const payload = JSON.parse(
        Buffer.from(accessToken.split('.')[1], 'base64').toString('utf-8')
      );
      userId = payload.sub || '';
      userRole = payload.role || 'USER';
    } catch (e) {
      console.error('JWT decoding failed:', e);
    }

    // 새로 발급받은 토큰으로 유저 정보 조회 (클라이언트 응답용)
    let user = null;
    try {
      const userResponse = await fetch(`${backendUrl}/users/me`, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });
      if (userResponse.ok) {
        const profile = await userResponse.json();
        // 백엔드 응답을 프론트엔드 스토어(User) 인터페이스에 맞게 매핑
        user = {
          id: profile.userId || userId,
          nickname: profile.nickname,
          email: profile.email || '',
          profileImage: profile.profileImageUrl,
          bio: profile.bio,
          role: profile.role || userRole,
        };
      }
    } catch (e) {
      console.error('BFF Fetch User Error:', e);
    }

    // 유저 정보 조회를 실패했더라도 세션용 데이터로 최소한의 정보 생성
    if (!user) {
      user = {
        id: userId,
        role: userRole,
        nickname: '사용자',
        email: ''
      };
    }

    // 3. iron-session에 토큰을 암호화하여 저장
    const response = NextResponse.json({ 
      success: true,
      isNewUser,
      user, // 클라이언트 스토어 동기화용 상세 유저 정보
    });

    const session = await getIronSession<SessionData>(request, response, sessionOptions);
    session.user = {
      id: userId,
      role: userRole,
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
