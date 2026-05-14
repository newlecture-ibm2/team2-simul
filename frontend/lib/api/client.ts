import axios, { AxiosRequestConfig, AxiosError } from 'axios';
import { toast } from '@/lib/utils/toast';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || '/api';

export const axiosInstance = axios.create({
  baseURL: API_BASE,
  timeout: 60000,
});

// Axios Interceptor for Response
axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config;

    // 401 Unauthorized 발생 시 토큰 갱신 시도 (동료의 코드 반영)
    if (
      error.response?.status === 401 &&
      originalRequest &&
      originalRequest.url !== '/auth/refresh' &&
      originalRequest.url !== '/auth/login/email' &&
      // 무한 루프 방지용 커스텀 플래그
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      !(originalRequest as any)._retry
    ) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (originalRequest as any)._retry = true;

      try {
        const refreshResponse = await fetch(`${API_BASE}/auth/refresh`, { method: 'POST' });
        
        if (refreshResponse.ok) {
          // 갱신 성공 시 원래 요청 재시도 (BFF가 세션을 업데이트했으므로 새 요청은 새 토큰 사용)
          return axiosInstance(originalRequest);
        }
      } catch (e) {
        console.error('Token refresh failed:', e);
      }
    }

    // 공통 에러 메시지 추출
    let errorMessage = error.message;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    if (error.response?.data && (error.response.data as any).message) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      errorMessage = (error.response.data as any).message;
    }

    // 자동으로 에러 토스트 띄우기 (401 갱신 실패 및 내 정보 조회 에러 등은 제외)
    if (
      error.response?.status !== 401 && 
      originalRequest?.url !== '/users/me'
    ) {
      toast.error(errorMessage);
    }

    return Promise.reject(new Error(errorMessage));
  }
);

interface FetchOptions extends Omit<RequestInit, 'body'> {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  params?: Record<string, any>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  body?: any;
}

/**
 * 공통 API wrapper
 * 기존 fetch 기반 코드를 호환하면서 Axios로 요청을 보냄
 */
export async function apiClient<T>(
  endpoint: string,
  options: FetchOptions = {}
): Promise<T> {
  const { params, method = 'GET', headers, body, ...rest } = options;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let data: any = body;
  if (typeof body === 'string') {
    try {
      data = JSON.parse(body);
    } catch {
      // JSON 파싱 실패 시 원본 유지
    }
  }

  const config: AxiosRequestConfig = {
    url: endpoint,
    method,
    params,
    headers: headers as Record<string, string>,
    data,
    // fetch의 RequestInit 옵션 중 호환 가능한 것들을 넘김 (예: signal)
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    ...(rest as any),
  };

  if (body instanceof FormData) {
    // FormData일 경우 헤더 처리 (Axios가 자동으로 경계(boundary)를 설정하므로 Content-Type을 지워줌)
    if (config.headers && config.headers['Content-Type']) {
      delete config.headers['Content-Type'];
    }
  }

  const response = await axiosInstance.request<T>(config);
  
  // 204 No Content 등 빈 응답 처리
  if (response.status === 204 || !response.data) {
    return undefined as T;
  }

  return response.data;
}
