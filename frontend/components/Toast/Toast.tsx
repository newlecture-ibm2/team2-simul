'use client';

import React, { useEffect, useState } from 'react';
import styles from './Toast.module.css';
import { toast, ToastPayload } from '@/lib/utils/toast';

export function Toast() {
  const [toastData, setToastData] = useState<ToastPayload | null>(null);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    let timer: NodeJS.Timeout;

    const unsubscribe = toast.listen((payload) => {
      setToastData(payload);
      setIsVisible(true);

      clearTimeout(timer);
      timer = setTimeout(() => {
        setIsVisible(false);
      }, 3000);
    });

    return () => {
      unsubscribe();
      clearTimeout(timer);
    };
  }, []);

  if (!toastData && !isVisible) return null;

  return (
    <div className={`${styles.toastWrapper} ${isVisible ? styles.visible : styles.hidden}`}>
      <div className={`${styles.toast} ${toastData ? styles[toastData.type] : ''}`}>
        {toastData?.message}
      </div>
    </div>
  );
}
