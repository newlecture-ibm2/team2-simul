'use client';

import React, { useState } from 'react';
import styles from './ClosetDetailModal.module.css';
import Button from '@/components/Button';
import Link from 'next/link';
import DeleteConfirmModal from '../DeleteConfirmModal/DeleteConfirmModal';
import FolderMoveModal from '../FolderMoveModal/FolderMoveModal';

const DUMMY_FOLDERS = [
  { id: 1, title: 'shirts outfit' },
  { id: 2, title: 'spring vibes' },
  { id: 3, title: 'wishlist' },
];

interface ClosetDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  itemId: number | null;
}

export default function ClosetDetailModal({ isOpen, onClose, itemId }: ClosetDetailModalProps) {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showMoveModal, setShowMoveModal] = useState(false);

  if (!isOpen || itemId === null) return null;

  // We only show the detail modal's internal content if move/delete modals aren't active
  // or if we want them to overlay. But the user said "modal should close".
  // To "close" and "open" another, we need to keep the component mounted if the other modal is a child.
  const showMainContent = !showMoveModal && !showDeleteConfirm;

  return (
    <>
      {showMainContent && (
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
                    <Link href="/tryon/studio" style={{ flex: 1 }}>
                      <Button variant="primary-dark" size="lg" fullWidth>
                        이 아이템으로 시착하기
                      </Button>
                    </Link>
                  </div>
                  <div className={styles.actionRow}>
                    <Button 
                      variant="secondary" 
                      size="lg" 
                      fullWidth
                      onClick={() => {
                        setShowMoveModal(true); 
                      }}
                    >
                      컬렉션에서 관리하기
                    </Button>
                  </div>
                  <div className={styles.dangerRow}>
                    <Button 
                      variant="secondary" 
                      size="lg" 
                      fullWidth
                      onClick={() => setShowDeleteConfirm(true)}
                    >
                      아이템 삭제
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      <DeleteConfirmModal 
        isOpen={showDeleteConfirm}
        count={1}
        onConfirm={() => {
          console.log('Delete item:', itemId);
          setShowDeleteConfirm(false);
          onClose();
        }}
        onCancel={() => setShowDeleteConfirm(false)}
      />

      <FolderMoveModal
        isOpen={showMoveModal}
        folders={DUMMY_FOLDERS}
        currentFolderId=""
        variant="fullScreen"
        onConfirm={(targetId) => {
          console.log(`Moving item ${itemId} to folder ${targetId}`);
          setShowMoveModal(true); // User might want to stay in move modal or close all
          setShowMoveModal(false);
          onClose();
        }}
        onCancel={() => setShowMoveModal(false)}
      />
    </>
  );
}
