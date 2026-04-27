'use client';

import React from 'react';
import styles from './ClosetDetailModal.module.css';
import Button from '@/components/Button';
import Link from 'next/link';

interface ClosetDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  itemId: number | null;
}

export default function ClosetDetailModal({ isOpen, onClose, itemId }: ClosetDetailModalProps) {
  if (!isOpen || itemId === null) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.modalInner}>
          <button className={styles.closeBtn} onClick={onClose} aria-label="닫기">
            ✕
          </button>

          <div className={styles.content}>
            <div className={styles.imageSection}>
              <img src="/clothes.png" alt="Clothes" className={styles.detailImage} />
            </div>

            <div className={styles.metaSection}>
              <button className={styles.editBtn} aria-label="메모 편집">
                <img src="/icons/pencil.png" alt="Edit" className={styles.editIcon} />
              </button>
              <p className={styles.memo}>
                봄에 입기 좋은 화이트 티셔츠. 데일리로 자주 입는 아이템.
              </p>
            </div>

            <div className={styles.actions}>
              <div className={styles.actionRow}>
                <Link href="/tryon/select-clothes" style={{ flex: 1 }}>
                  <Button variant="primary" size="lg" fullWidth>
                    이 옷으로 시착하기
                  </Button>
                </Link>
              </div>
              <div className={styles.dangerRow}>
                <Button variant="secondary" size="lg" fullWidth>
                  아이템 삭제
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
