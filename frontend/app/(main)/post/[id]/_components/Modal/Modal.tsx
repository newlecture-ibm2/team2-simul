'use client';

import styles from './Modal.module.css';

interface ModalProps {
  isOpen: boolean;
  title?: string;
  message: string;
  onConfirm: () => void;
  onCancel?: () => void;
  confirmText?: string;
  cancelText?: string;
}

export default function Modal({ isOpen, title, message, onConfirm, onCancel, confirmText = '확인', cancelText = '취소' }: ModalProps) {
  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onCancel || onConfirm}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        {title && <h2 className={styles.title}>{title}</h2>}
        <p className={styles.message}>{message}</p>
        <div className={styles.actions}>
          {onCancel && (
            <button className={`${styles.btn} ${styles.cancelBtn}`} onClick={onCancel}>{cancelText}</button>
          )}
          <button className={`${styles.btn} ${styles.confirmBtn}`} onClick={onConfirm}>{confirmText}</button>
        </div>
      </div>
    </div>
  );
}
