import { NextRequest, NextResponse } from 'next/server';
import { getIronSession } from 'iron-session';
import { sessionOptions, SessionData } from '@/lib/session';

/**
 * Local debugging endpoint to verify whether iron-session contains JWT tokens.
 * Do not expose sensitive token values.
 */
export async function GET(request: NextRequest) {
  const response = NextResponse.json({ ok: true });
  const session = await getIronSession<SessionData>(request, response, sessionOptions);

  return NextResponse.json({
    hasSessionUser: !!session.user,
    hasAccessToken: !!session.user?.token,
    role: session.user?.role ?? null,
  });
}

