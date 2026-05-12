'use client';

import { useState } from 'react';
import styles from './ReportModal.module.css';

interface ReportModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (reason: string) => void;
  isSubmitting: boolean;
}

const REPORT_REASONS = [
  { id: 'SPAM', label: '스팸 및 홍보성 내용' },
  { id: 'INAPPROPRIATE', label: '부적절한 콘텐츠' },
  { id: 'ABUSIVE', label: '욕설 및 비하 발언' },
  { id: 'COPYRIGHT', label: '저작권 침해' },
  { id: 'OTHER', label: '기타 (직접 입력)' },
];

export default function ReportModal({ isOpen, onClose, onSubmit, isSubmitting }: ReportModalProps) {
  const [selectedReasonId, setSelectedReasonId] = useState<string>('');
  const [otherReason, setOtherReason] = useState('');

  if (!isOpen) return null;

  const handleSubmit = () => {
    if (selectedReasonId === 'OTHER') {
      onSubmit(otherReason);
    } else {
      const selected = REPORT_REASONS.find((r) => r.id === selectedReasonId);
      if (selected) onSubmit(selected.label);
    }
  };

  const isSubmitDisabled = 
    !selectedReasonId || 
    (selectedReasonId === 'OTHER' && !otherReason.trim()) || 
    isSubmitting;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <h3 className={styles.title}>게시물 신고하기</h3>
        <p className={styles.description}>
          해당 게시물이 커뮤니티 가이드를 위반했다고 생각되시면 신고해주세요. 누적 10회 이상 신고된 게시물은 자동으로 블라인드 처리됩니다.
        </p>
        
        <div className={styles.reasonList}>
          {REPORT_REASONS.map((reason) => (
            <label key={reason.id} className={styles.radioLabel}>
              <input
                type="radio"
                name="reportReason"
                value={reason.id}
                checked={selectedReasonId === reason.id}
                onChange={(e) => setSelectedReasonId(e.target.value)}
                className={styles.radioInput}
              />
              <span className={styles.radioText}>{reason.label}</span>
            </label>
          ))}
        </div>
        
        {selectedReasonId === 'OTHER' && (
          <textarea
            className={styles.textarea}
            placeholder="신고 사유를 구체적으로 작성해주세요 (최대 200자)"
            value={otherReason}
            onChange={(e) => setOtherReason(e.target.value)}
            maxLength={200}
          />
        )}
        
        <div className={styles.actions}>
          <button className={`${styles.btn} ${styles.cancelBtn}`} onClick={onClose} disabled={isSubmitting}>
            취소
          </button>
          <button 
            className={`${styles.btn} ${styles.submitBtn}`} 
            onClick={handleSubmit}
            disabled={isSubmitDisabled}
          >
            {isSubmitting ? '처리 중...' : '신고 접수'}
          </button>
        </div>
      </div>
    </div>
  );
}
