import { NextRequest, NextResponse } from 'next/server';
import { proxyHandler } from './handlers/proxyHandler';

type Context = { params: Promise<{ path: string[] }> };

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
