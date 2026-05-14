import { NextRequest, NextResponse } from 'next/server';
import { getIronSession } from 'iron-session';
import { sessionOptions, SessionData } from '@/lib/session';
import { cookies } from 'next/headers';
import { proxyHandler } from '../../[...path]/handlers/proxyHandler';

export async function GET(request: NextRequest) {
  return proxyHandler(request, ['users', 'me']);
}

export async function PATCH(request: NextRequest) {
  return proxyHandler(request, ['users', 'me']);
}

/**
 * 회원 탈퇴 BFF 라우트
 * DELETE /api/users/me
 */
export async function DELETE(request: NextRequest) {
  const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';

  // 1. 세션 가져오기
  const response = NextResponse.json({ success: true });
  const session = await getIronSession<SessionData>(await cookies(), sessionOptions);
  
  const accessToken = session.user?.token;
  
  if (!accessToken) {
    console.error('[Withdraw] No access token found in session');
    return NextResponse.json(
      { error_code: 'ERR-001', message: '인증 정보가 없습니다.' },
      { status: 401 }
    );
  }

  try {
    // 2. 백엔드 회원 탈퇴 API 호출
    const backendResponse = await fetch(`${backendUrl}/users/me`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
    });

    if (!backendResponse.ok) {
      const errorText = await backendResponse.text();
      console.error(`[Withdraw] Backend error (${backendResponse.status}):`, errorText);
      
      try {
        const errorData = JSON.parse(errorText);
        return NextResponse.json(errorData, { status: backendResponse.status });
      } catch {
        return NextResponse.json(
          { error_code: 'ERR-000', message: '백엔드 처리 중 오류 발생', detail: errorText },
          { status: backendResponse.status }
        );
      }
    }

    // 3. 세션 파괴 (로그아웃 처리)
    await session.destroy();
    
    return response;
  } catch (error) {
    console.error('[Withdraw] Backend call failed:', error);
    return NextResponse.json(
      { error_code: 'ERR-000', message: '백엔드 서버 연결 실패' },
      { status: 502 }
    );
  }
}
