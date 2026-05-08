'use client';

import { usePathname } from 'next/navigation';
import { useEffect } from 'react';

// 클라이언트 사이드 라우팅 간 이전 뎁스를 기억하기 위한 전역 변수
let previousDepth = 0;

function getDepth(pathname: string) {
  const rootPaths = ['/', '/closet', '/tryon', '/profile'];
  if (rootPaths.includes(pathname)) {
    return 1; // 하단 탭바가 있는 메인 화면들은 모두 뎁스 1
  }
  
  // 그 외의 경로는 URL segment 개수로 깊이를 계산 (예: /post/123 -> segments 2개 -> 뎁스 3)
  const segments = pathname.split('/').filter(Boolean);
  return segments.length + 1;
}

export default function Template({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const currentDepth = getDepth(pathname);
  
  let animationClass = ""; // 기본: 애니메이션 없음 (동등한 뎁스 이동 시)

  // 탭바 클릭을 통한 이동인지 확인 (Strict Mode 대응: 렌더링 중에는 읽기만 수행)
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const isBottomNavClick = typeof window !== 'undefined' ? (window as any).isNavigatingFromBottomNav : false;

  if (!isBottomNavClick && previousDepth > 0) {
    if (currentDepth > previousDepth) {
      // 안쪽으로 깊게 이동할 때 (오른쪽에서 밀려 들어옴)
      animationClass = "animate-slide-in-right";
    } else if (currentDepth < previousDepth) {
      // 바깥쪽으로 빠져나올 때 (왼쪽에서 밀려 들어옴)
      animationClass = "animate-slide-in-left";
    }
  }

  // 렌더링 후 상태 업데이트
  useEffect(() => {
    previousDepth = currentDepth;
    // 마운트 완료 후 탭바 클릭 플래그 초기화
    if (typeof window !== 'undefined') {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (window as any).isNavigatingFromBottomNav = false;
    }
  }, [currentDepth]);

  return (
    <div className={animationClass}>
      {children}
    </div>
  );
}
