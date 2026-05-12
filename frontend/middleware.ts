import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';
import { getIronSession } from 'iron-session';
import { sessionOptions, SessionData } from '@/lib/session';

const ADMIN_ROUTES = ['/admin'];
const AUTH_REQUIRED_ROUTES = ['/closet', '/profile', '/settings', '/tryon', '/post/create'];

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const res = NextResponse.next();

  // iron-session v8에서는 request와 response 객체를 넘겨주어 세션을 얻을 수 있습니다.
  const session = await getIronSession<SessionData>(request, res, sessionOptions);
  const user = session.user;

  // 1. Admin 라우트 권한 검증
  const isAdminRoute = ADMIN_ROUTES.some((route) => pathname.startsWith(route));
  if (isAdminRoute) {
    if (!user) {
      // 미로그인 상태면 로그인 페이지로
      const loginUrl = new URL('/login', request.url);
      loginUrl.searchParams.set('returnUrl', pathname);
      return NextResponse.redirect(loginUrl);
    }
    if (user.role !== 'ADMIN') {
      // 관리자가 아니면 홈으로 리다이렉트
      return NextResponse.redirect(new URL('/', request.url));
    }
  }

  // 2. 일반 유저 인증 필수 라우트 검증
  const isAuthRoute = AUTH_REQUIRED_ROUTES.some((route) => pathname.startsWith(route));
  if (isAuthRoute && !user) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('returnUrl', pathname);
    return NextResponse.redirect(loginUrl);
  }

  // 기타 접근은 통과 (API 핸들러 등은 별도 검증)
  return res;
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes -> proxyHandler에서 검증)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - uploads (local images)
     */
    '/((?!api|_next/static|_next/image|favicon.ico|uploads).*)',
  ],
};
