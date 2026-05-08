const API_BASE = process.env.NEXT_PUBLIC_API_URL || '/api';

interface FetchOptions extends RequestInit {
  params?: Record<string, string>;
}

/**
 * 공통 fetch wrapper — BFF 패턴 대비
 * Next.js API Routes를 통해 Spring Boot 백엔드와 통신
 */
export async function apiClient<T>(
  endpoint: string,
  options: FetchOptions = {}
): Promise<T> {
  const { params, ...fetchOptions } = options;

  let url = `${API_BASE}${endpoint}`;

  if (params) {
    const searchParams = new URLSearchParams(params);
    url += `?${searchParams.toString()}`;
  }

  const headers: HeadersInit = {
    ...fetchOptions.headers,
  };

  if (!(fetchOptions.body instanceof FormData) && !headers['Content-Type' as keyof typeof headers]) {
    (headers as Record<string, string>)['Content-Type'] = 'application/json';
  }

  const response = await fetch(url, {
    ...fetchOptions,
    headers,
  });

  // 401 Unauthorized 발생 시 토큰 갱신 시도
  if (response.status === 401 && endpoint !== '/auth/refresh' && endpoint !== '/auth/login/email') {
    try {
      const refreshResponse = await fetch(`${API_BASE}/auth/refresh`, { method: 'POST' });
      
      if (refreshResponse.ok) {
        // 갱신 성공 시 원래 요청 재시도 (BFF가 세션 업데이트했으므로 새 요청은 새 토큰 사용)
        return apiClient(endpoint, options);
      }
    } catch (e) {
      console.error('Token refresh failed:', e);
    }
  }

  if (!response.ok) {
    let errorMessage = `API Error: ${response.status} ${response.statusText}`;
    try {
      const errorData = await response.json();
      if (errorData.message) {
        errorMessage = errorData.message;
      }
    } catch {
      // JSON 파싱 실패 시 기본 메시지 유지
    }
    throw new Error(errorMessage);
  }

  // 204 No Content 등 빈 응답 처리
  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return undefined as T;
  }

  return response.json();
}
