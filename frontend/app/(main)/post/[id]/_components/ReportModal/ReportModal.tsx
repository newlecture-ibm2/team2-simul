'use client';

import { useState } from 'react';
import styles from './ReportModal.module.css';

interface ReportModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (reason: string) => void;
  isSubmitting: boolean;
}

export default function ReportModal({ isOpen, onClose, onSubmit, isSubmitting }: ReportModalProps) {
  const [reason, setReason] = useState('');

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <h3 className={styles.title}>게시물 신고하기</h3>
        <p className={styles.description}>
          해당 게시물이 커뮤니티 가이드를 위반했다고 생각되시면 신고해주세요. 누적 5회 이상 신고된 게시물은 자동으로 블라인드 처리됩니다.
        </p>
        
        <textarea
          className={styles.textarea}
          placeholder="신고 사유를 구체적으로 작성해주세요 (최대 200자)"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          maxLength={200}
        />
        
        <div className={styles.actions}>
          <button className={`${styles.btn} ${styles.cancelBtn}`} onClick={onClose} disabled={isSubmitting}>
            취소
          </button>
          <button 
            className={`${styles.btn} ${styles.submitBtn}`} 
            onClick={() => onSubmit(reason)}
            disabled={!reason.trim() || isSubmitting}
          >
            {isSubmitting ? '처리 중...' : '신고 접수'}
          </button>
        </div>
      </div>
    </div>
  );
}
