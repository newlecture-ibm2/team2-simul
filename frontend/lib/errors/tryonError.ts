type TryonErrorInput = {
  code?: string;
  status?: number;
  fallbackMessage?: string;
};

export function getTryonErrorMessage({ code, status, fallbackMessage }: TryonErrorInput): string {
  if (code === 'ERR-103-A') {
    return '오늘의 무료 시착 크레딧을 모두 사용했어요. 24시(KST)에 다시 사용할 수 있습니다.';
  }
  if (code === 'ERR-103-B') {
    return '시착 이미지 생성에 실패했어요. 잠시 후 다시 시도해주세요.';
  }
  if (code === 'ERR-103-C' || status === 408) {
    return '생성이 지연되고 있어요. 잠시 후 다시 시도해주세요.';
  }
  if (code === 'ERR-103-D') {
    return '이미지 정책상 시착할 수 없는 사진입니다. 다른 사진으로 시도해주세요.';
  }
  if (status === 429) {
    return '요청이 잠시 많아요. 잠시 후 다시 시도해주세요.';
  }
  if (status === 401) {
    return '로그인이 만료되었습니다. 다시 로그인 후 시도해주세요.';
  }
  if (status === 403) {
    return '이 작업에 대한 권한이 없습니다.';
  }
  if (status === 404) {
    return '요청한 시착 정보를 찾지 못했습니다.';
  }

  return fallbackMessage || '시착 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
}
