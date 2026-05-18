'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import BottomSheet from '@/components/BottomSheet';
import Button from '@/components/Button';
import styles from './AuthBottomSheet.module.css';

const AUTH_MODAL_EVENT = 'simul_auth_modal_event';

export default function AuthBottomSheet() {
  const [isOpen, setIsOpen] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const handleOpen = (e: Event) => {
      const customEvent = e as CustomEvent<{ isOpen: boolean }>;
      if (customEvent.detail.isOpen) {
        setIsOpen(true);
      }
    };

    window.addEventListener(AUTH_MODAL_EVENT, handleOpen);
    return () => window.removeEventListener(AUTH_MODAL_EVENT, handleOpen);
  }, []);

  const handleClose = () => {
    setIsOpen(false);
  };

  const handleLoginClick = () => {
    setIsOpen(false);
    router.push('/login');
  };

  return (
    <BottomSheet isOpen={isOpen} onClose={handleClose} title="로그인 안내">
      <div className={styles.container}>
        <p className={styles.message}>
          이 기능은 로그인이 필요해요.<br />
          간편하게 로그인하고 더 많은 기능을 즐겨보세요!
        </p>
        <div className={styles.buttonGroup}>
          <Button variant="secondary" onClick={handleClose}>
            나중에
          </Button>
          <Button variant="primary" onClick={handleLoginClick}>
            로그인하기
          </Button>
        </div>
      </div>
    </BottomSheet>
  );
}
