import { NextRequest, NextResponse } from 'next/server';
import { getIronSession } from 'iron-session';
import { sessionOptions, SessionData } from '@/lib/session';
import { cookies } from 'next/headers';

/**
 * 인증 관련 요청 핸들러 (BFF)
 * 백엔드에서 받은 JWT를 iron-session 쿠키에 저장
 */
interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  isNewUser?: boolean;
}

export async function authHandler(req: NextRequest, path: string[]) {
  const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';
  const targetUrl = BACKEND_URL + '/' + path.join('/') + req.nextUrl.search;

  // Preflight (OPTIONS) 요청 처리
  if (req.method === 'OPTIONS') {
    return new NextResponse(null, {
      status: 204,
      headers: {
        'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization',
        'Access-Control-Max-Age': '86400',
      },
    });
  }

  console.log(`[BFF Auth] Forwarding ${req.method} request to: ${targetUrl}`);

  try {
    // 원본 헤더 복사 및 호스트 설정
    const headers = new Headers(req.headers);
    headers.set('host', new URL(BACKEND_URL).host);
    headers.delete('connection');
    headers.delete('keep-alive');

    // 세션 정보 가져오기 (토큰 추출 및 로그아웃/갱신 처리용)
    const session = await getIronSession<SessionData>(req, new Response(), sessionOptions);

    // 1. 토큰 갱신(refresh) 요청인 경우 세션에서 리프레시 토큰 주입
    let requestBody = req.method !== 'GET' && req.method !== 'HEAD' ? await req.text() : undefined;
    if (path.includes('refresh') && !requestBody) {
      const refreshToken = session.user?.refreshToken;
      if (refreshToken) {
        requestBody = JSON.stringify({ refreshToken });
        headers.set('Content-Type', 'application/json');
        console.log('[BFF Auth] Injecting refreshToken from session for renewal');
      }
    }

    const response = await fetch(targetUrl, {
      method: req.method,
      headers,
      body: requestBody,
    });

    // 응답 본문이 있는지 확인 후 파싱
    const contentType = response.headers.get('content-type');
    let data: AuthResponse = {} as AuthResponse;
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    }

    // 1. 로그아웃 처리: path에 logout이 포함되어 있고 성공했다면 세션 파기
    if (path.includes('logout') && response.ok) {
      const res = NextResponse.json({ success: true });
      const session = await getIronSession<SessionData>(req, res, sessionOptions);
      session.destroy();
      console.log('[BFF Auth] Logout success, session destroyed');
      return res;
    }

    // 2. 로그인 성공 처리: accessToken이 있는 경우 세션 생성
    if (response.ok && data.accessToken) {
      const accessToken = data.accessToken;
      const refreshToken = data.refreshToken;
      
      // 1. JWT payload에서 userId 추출
      let userId = 'unknown';
      try {
        const payload = JSON.parse(Buffer.from(accessToken.split('.')[1], 'base64').toString('utf-8'));
        userId = payload.sub || 'unknown';
      } catch (e) {
        console.error('[BFF Auth] JWT decode failed', e);
      }

      // 2. 로그인 성공 직후 유저 정보 조회 (캐시 업데이트용)
      let profileData = { userId, nickname: '사용자', role: 'USER', profileImageUrl: '' };
      try {
        const userRes = await fetch(`${BACKEND_URL}/users/me`, {
          headers: { 'Authorization': `Bearer ${accessToken}` }
        });
        if (userRes.ok) {
          profileData = await userRes.json();
        }
      } catch (e) {
        console.error('[BFF Auth] Fetch user info failed', e);
      }

      // 3. 세션 생성 및 저장
      const res = NextResponse.json({ ...data, user: profileData });
      const session = await getIronSession<SessionData>(req, res, sessionOptions);
      
      session.user = {
        id: profileData.userId || userId,
        role: (profileData.role as 'USER' | 'ADMIN') || 'USER',
        token: accessToken,
        refreshToken: refreshToken,
      };
      
      await session.save();
      console.log(`[BFF Auth] Login success: ${profileData.nickname} (${userId})`);
      return res;
    }

    // 3. 리프레시 실패 처리: 토큰 갱신 시도 중 401/403이 발생하면 세션 파기
    if (path.includes('refresh') && (response.status === 401 || response.status === 403)) {
      const res = NextResponse.json(data, { status: response.status });
      const session = await getIronSession<SessionData>(req, res, sessionOptions);
      session.destroy();
      console.log('[BFF Auth] Refresh failed, session destroyed');
      return res;
    }

    return NextResponse.json(data, { status: response.status });
  } catch (error) {
    console.error('[BFF Auth Error]', error);
    return NextResponse.json(
      { error_code: 'ERR-000', message: '인증 서버 연결 실패' },
      { status: 502 }
    );
  }
}
