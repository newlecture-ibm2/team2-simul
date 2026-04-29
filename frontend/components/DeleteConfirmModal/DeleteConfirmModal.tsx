'use client';

import styles from './DeleteConfirmModal.module.css';

interface DeleteConfirmModalProps {
  isOpen: boolean;
  count: number;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function DeleteConfirmModal({
  isOpen,
  count,
  onConfirm,
  onCancel,
}: DeleteConfirmModalProps) {
  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onCancel}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>


        <h2 className={styles.title}>삭제하시겠습니까?</h2>
        <p className={styles.description}>
          선택한 {count}개의 아이템이 영구적으로 삭제됩니다.
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
    </div>
  );
}
