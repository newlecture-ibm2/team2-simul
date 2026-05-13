'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './LoginRequiredBottomSheet.module.css';

interface LoginRequiredBottomSheetProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function LoginRequiredBottomSheet({ isOpen, onClose }: LoginRequiredBottomSheetProps) {
  const router = useRouter();
  const [isRendered, setIsRendered] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setIsRendered(true);
    } else {
      const timer = setTimeout(() => setIsRendered(false), 300); // Wait for animation
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  if (!isRendered) return null;

  return (
    <div className={`${styles.overlay} ${isOpen ? styles.open : ''}`} onClick={onClose}>
      <div 
        className={`${styles.bottomSheet} ${isOpen ? styles.open : ''}`} 
        onClick={(e) => e.stopPropagation()}
      >
        <div className={styles.handle} />
        
        <h3 className={styles.title}>로그인이 필요한 서비스입니다</h3>
        <p className={styles.description}>
          로그인하고 Simul의 AI 가상시착과 다양한 기능을 자유롭게 이용해 보세요.
        </p>
        
        <div className={styles.actions}>
          <button className={`${styles.btn} ${styles.loginBtn}`} onClick={() => router.push('/login')}>
            로그인하러 가기
          </button>
          <button className={`${styles.btn} ${styles.cancelBtn}`} onClick={onClose}>
            다음에 하기
          </button>
        </div>
      </div>
    </div>
  );
}
