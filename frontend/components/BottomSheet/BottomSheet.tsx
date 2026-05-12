'use client';

import React, { useEffect } from 'react';
import styles from './BottomSheet.module.css';

interface BottomSheetProps {
  /** 바텀시트 열림 여부 */
  isOpen: boolean;
  /** 바텀시트 닫기 콜백 함수 */
  onClose: () => void;
  /** 선택적 타이틀 */
  title?: string;
  /** 내부 콘텐츠 */
  children: React.ReactNode;
}

/**
 * 기본형 바텀시트 레퍼런스 컴포넌트
 * - 사용 시 이 컴포넌트를 직접 import하지 말고, 필요한 페이지의 _components/ 로 복사하여 커스텀할 것
 */
export default function BottomSheet({ isOpen, onClose, title, children }: BottomSheetProps) {
  // 모달 활성화 시 Body 스크롤 방지
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose} role="dialog" aria-modal="true">
      <div 
        className={styles.sheet} 
        onClick={(e) => e.stopPropagation()} // 내부 클릭 시 닫힘 방지
      >
        <div className={styles.handle} aria-hidden="true" />
        
        {title && (
          <div className={styles.header}>
            <h2 className={styles.title}>{title}</h2>
          </div>
        )}
        
        <div className={styles.content}>
          {children}
        </div>
      </div>
    </div>
  );
}
