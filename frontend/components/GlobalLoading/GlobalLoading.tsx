'use client';

import React, { useEffect, useState } from 'react';
import { useIsFetching, useIsMutating } from '@tanstack/react-query';
import styles from './GlobalLoading.module.css';

export function GlobalLoading() {
  const isFetching = useIsFetching();
  const isMutating = useIsMutating();
  const [showSpinner, setShowSpinner] = useState(false);

  const isLoading = isFetching > 0 || isMutating > 0;

  useEffect(() => {
    let timer: NodeJS.Timeout;

    if (isLoading) {
      // 깜빡임 방지용 300ms 딜레이
      timer = setTimeout(() => {
        setShowSpinner(true);
      }, 300);
    } else {
      setShowSpinner(false);
    }

    return () => clearTimeout(timer);
  }, [isLoading]);

  if (!showSpinner) return null;

  return (
    <div className={styles.overlay}>
      <div className={styles.spinner}></div>
    </div>
  );
}
