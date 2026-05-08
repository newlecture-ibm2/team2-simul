import axios, { AxiosRequestConfig, AxiosError } from 'axios';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || '/api';

export const axiosInstance = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
});

// Axios Interceptor for Response
axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    // TODO: 다른 팀원이 토큰 만료(401) 시 자동 갱신(Refresh) 로직을 구현할 곳
    if (error.response?.status === 401) {
      console.warn('토큰이 만료되었습니다. (갱신 로직 구현 예정)');
      // 예: /auth/refresh 호출 후 원래 요청(error.config) 재시도
    }

    // 공통 에러 메시지 추출
    let errorMessage = error.message;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    if (error.response?.data && (error.response.data as any).message) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      errorMessage = (error.response.data as any).message;
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
    } catch (e) {
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
    ...rest,
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
