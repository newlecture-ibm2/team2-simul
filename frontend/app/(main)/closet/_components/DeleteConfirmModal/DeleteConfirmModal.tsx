'use client';

import React, { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import styles from './DeleteConfirmModal.module.css';

interface DeleteConfirmModalProps {
  isOpen: boolean;
  count: number;
  title?: string;
  description?: string;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function DeleteConfirmModal({
  isOpen,
  count,
  title = '삭제하시겠습니까?',
  description,
  onConfirm,
  onCancel,
}: DeleteConfirmModalProps) {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!isOpen || !mounted) return null;

  const displayDescription = description || `선택한 ${count}개의 아이템이 영구적으로 삭제됩니다.`;

  return createPortal(
    <div className={styles.overlay} onClick={onCancel}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>


        <h2 className={styles.title}>{title}</h2>
        <p className={styles.description}>
          {displayDescription}
        </p>

        <div className={styles.actions}>
          <button className={`${styles.btn} ${styles.cancelBtn}`} onClick={onCancel}>
            아니오
          </button>
          <button className={`${styles.btn} ${styles.confirmBtn}`} onClick={onConfirm}>
            예
          </button>
        </div>
      </div>
    </div>,
    document.body
  );
}
