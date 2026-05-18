import { NextRequest } from 'next/server';
import { proxyHandler } from './handlers/proxyHandler';
import { authHandler } from './handlers/authHandlers';

type Context = { params: Promise<{ path: string[] }> };

// Ensure Node.js runtime for streaming (SSE) proxying.
export const runtime = 'nodejs';
export const dynamic = 'force-dynamic';

/**
 * BFF Catch-all 라우터
 * /api/* 요청을 Spring Boot 백엔드로 프록시
 */

function getHandler(path: string[]) {
  // 인증 관련 경로는 별도 핸들러 사용 (세션 저장 필요)
  if (path[0] === 'auth') {
    return authHandler;
  }
  return proxyHandler;
}

export async function GET(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return getHandler(path)(req, path);
}

export async function POST(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return getHandler(path)(req, path);
}

export async function PUT(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return getHandler(path)(req, path);
}

export async function PATCH(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return getHandler(path)(req, path);
}

export async function DELETE(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return getHandler(path)(req, path);
}

export async function OPTIONS(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return getHandler(path)(req, path);
}
