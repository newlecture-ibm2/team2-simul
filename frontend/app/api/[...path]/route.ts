import { NextRequest } from 'next/server';
import { proxyHandler } from './handlers/proxyHandler';

type Context = { params: Promise<{ path: string[] }> };

// Ensure Node.js runtime for streaming (SSE) proxying.
export const runtime = 'nodejs';
export const dynamic = 'force-dynamic';

/**
 * BFF Catch-all 라우터
 * /api/* 요청을 Spring Boot 백엔드로 프록시
 */

export async function GET(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return proxyHandler(req, path);
}

export async function POST(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return proxyHandler(req, path);
}

export async function PUT(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return proxyHandler(req, path);
}

export async function PATCH(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return proxyHandler(req, path);
}

export async function DELETE(req: NextRequest, context: Context) {
  const { path } = await context.params;
  return proxyHandler(req, path);
}
