import { NextRequest } from 'next/server';

/**
 * BFF 프록시 핸들러
 * 브라우저 → /api/... → Spring Boot 백엔드로 프록시
 */

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function proxyHandler(req: NextRequest, path: string[]) {
  // 타겟 URL 구성 (백엔드는 /api 접두사 없음)
  const targetUrl = BACKEND_URL + '/' + path.join('/') + req.nextUrl.search;
  
  try {
    const fetchOptions: RequestInit = {
      method: req.method,
      headers: {
        ...Object.fromEntries(req.headers.entries()),
        host: new URL(BACKEND_URL).host,
        'Content-Type': req.headers.get('Content-Type') || 'application/json',
      },
      redirect: 'manual',
      cache: 'no-store',
    };

    // TODO: iron-session 도입 시 여기서 JWT 추출하여 Authorization 헤더 주입

    if (req.method !== 'GET' && req.method !== 'HEAD') {
      const body = await req.arrayBuffer();
      if (body.byteLength > 0) {
        fetchOptions.body = body;
      }
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
