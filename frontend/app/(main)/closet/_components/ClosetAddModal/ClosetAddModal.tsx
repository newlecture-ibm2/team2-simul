'use client';

import { useState } from 'react';
import styles from './ClosetAddModal.module.css';
import Button from '@/components/Button';

interface ClosetAddModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: { memo: string }) => void;
}

export default function ClosetAddModal({ isOpen, onClose, onSave }: ClosetAddModalProps) {
  const [memo, setMemo] = useState('');

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.modalInner}>
          <button className={styles.closeBtn} onClick={onClose} aria-label="닫기">✕</button>
          
          <div className={styles.content}>
            <h2 className={styles.title}>아이템 추가</h2>
            
            <div className={styles.uploadGroup}>
              <button className={styles.uploadBtn}>
                <img src="/icons/camera.png" alt="촬영" className={styles.icon} />
                <span className={styles.btnText}>지금 촬영</span>
              </button>
              <button className={styles.uploadBtn}>
                <img src="/icons/photo.on.rectangle.angled.png" alt="업로드" className={styles.icon} />
                <span className={styles.btnText}>사진 업로드</span>
              </button>
            </div>

            <div className={styles.inputGroup}>
              <label className={styles.label}>메모</label>
              <textarea 
                className={styles.textarea}
                placeholder="아이템에 대한 메모를 남겨보세요 (선택사항)"
                value={memo}
                onChange={(e) => setMemo(e.target.value)}
              />
            </div>

            <div className={styles.footer}>
              <Button variant="secondary" size="lg" fullWidth onClick={onClose}>
                취소
              </Button>
              <Button 
                variant="primary" 
                size="lg" 
                fullWidth 
                onClick={() => {
                  onSave({ memo });
                  onClose();
                }}
              >
                저장
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
