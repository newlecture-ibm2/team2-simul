import { NextRequest } from 'next/server';
import { getIronSession } from 'iron-session';
import { sessionOptions, SessionData } from '@/lib/session';

/**
 * BFF 프록시 핸들러
 * 브라우저 → /api/... → Spring Boot 백엔드로 프록시
 */

export async function proxyHandler(req: NextRequest, path: string[]) {
  // 함수 내부에 두어야 Next.js가 빌드 타임에 상수로 치환(하드코딩)하지 않고 런타임 환경변수를 읽어옵니다.
  const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';
  // 타겟 URL 구성 (백엔드는 /api 접두사 없음)
  const targetUrl = BACKEND_URL + '/' + path.join('/') + req.nextUrl.search;

  try {
    // 원본 헤더를 복사하되, host 헤더는 백엔드에 맞게 변경
    const headers = new Headers(req.headers);
    headers.set('host', new URL(BACKEND_URL).host);

    // ⚠️ Content-Type을 절대 덮어쓰지 않음!
    // multipart/form-data의 경우 브라우저가 생성한 boundary가 포함되어 있어야 함
    // 예: "multipart/form-data; boundary=----WebKitFormBoundary..."

    const fetchOptions: RequestInit & { duplex?: 'half' } = {
      method: req.method,
      headers,
      redirect: 'manual',
      cache: 'no-store',
    };

    // iron-session을 통해 세션의 JWT 추출 후 Authorization 헤더 주입
    const session = await getIronSession<SessionData>(req, new Response(), sessionOptions);
    if (session.user?.token) {
      headers.set('Authorization', `Bearer ${session.user.token}`);
    }

    if (req.method !== 'GET' && req.method !== 'HEAD' && req.body) {
      fetchOptions.body = req.body;
      fetchOptions.duplex = 'half'; // Node.js 18+ fetch에서 ReadableStream을 body로 보낼 때 필수
    }

    const response = await fetch(targetUrl, fetchOptions);

    const resHeaders = new Headers(response.headers);
    resHeaders.delete('content-encoding');

    return new Response(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers: resHeaders,
    });
  } catch (error) {
    console.error('[BFF Proxy Error]', error);
    return new Response(
      JSON.stringify({
        error_code: 'ERR-000',
        message: '백엔드 서버 연결 실패',
        detail: error instanceof Error ? error.message : 'Unknown error'
      }),
      {
        status: 502,
        headers: { 'Content-Type': 'application/json' }
      }
    );
  }
}
