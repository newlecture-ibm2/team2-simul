'use client';

import { useState } from 'react';
import styles from './FolderMoveModal.module.css';

interface Folder {
  id: string | number;
  title: string;
}

interface FolderMoveModalProps {
  isOpen: boolean;
  folders: Folder[];
  currentFolderId?: string | number;
  onConfirm: (targetFolderId: string | number) => void;
  onCancel: () => void;
  variant?: 'popover' | 'fullScreen';
}

export default function FolderMoveModal({
  isOpen,
  folders,
  currentFolderId = '',
  onConfirm,
  onCancel,
  variant = 'popover',
}: FolderMoveModalProps) {
  const [selectedId, setSelectedId] = useState<string | number>(currentFolderId);

  if (!isOpen) return null;

  return (
    <div className={`${styles.overlay} ${variant === 'fullScreen' ? styles.fullScreenOverlay : ''}`} onClick={onCancel}>
      <div 
        className={`${styles.modal} ${variant === 'fullScreen' ? styles.fullScreenModal : ''}`} 
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className={styles.title}>이동할 폴더 선택</h2>
        
        <div className={styles.folderList}>
          {folders.map((folder) => (
            <div
              key={folder.id}
              className={`${styles.folderItem} ${selectedId === folder.id ? styles.selected : ''}`}
              onClick={() => setSelectedId(folder.id)}
            >
              <div className={styles.folderInfo}>
                <img src="/icons/folder.png" alt="" className={styles.folderIcon} />
                <span className={styles.folderName}>{folder.title}</span>
              </div>
              <div className={styles.radioCircle}>
                <div className={styles.radioInner} />
              </div>
            </div>
          ))}
        </div>

        <div className={styles.actions}>
          <button className={`${styles.btn} ${styles.cancelBtn}`} onClick={onCancel}>
            취소
          </button>
          <button
            className={`${styles.btn} ${styles.confirmBtn}`}
            onClick={() => selectedId && onConfirm(selectedId)}
            disabled={!selectedId}
          >
            이동
          </button>
        </div>
      </div>
    </div>
  );
}
