import { NextRequest } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function proxyHandler(req: NextRequest, path: string[]) {
  const targetUrl = BACKEND_URL + '/api/' + path.join('/') + req.nextUrl.search;
  
  try {
    const fetchOptions: RequestInit = {
      method: req.method,
      headers: {
        ...Object.fromEntries(req.headers.entries()),
        host: new URL(BACKEND_URL).host,
      },
      redirect: 'manual',
      cache: 'no-store',
    };

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
    console.error('BFF Proxy Error:', error);
    return new Response(
      JSON.stringify({
        error_code: 'ERR-000',
        message: 'BFF Proxy Error',
        detail: error instanceof Error ? error.message : 'Unknown error'
      }),
      { status: 502, headers: { 'Content-Type': 'application/json' } }
    );
  }
}
