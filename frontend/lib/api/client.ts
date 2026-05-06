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

  if (!response.ok) {
    throw new Error(`API Error: ${response.status} ${response.statusText}`);
  }

  // 204 No Content 등 빈 응답 처리
  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return undefined as T;
  }

  return response.json();
}
