'use client';

import React, { useState } from 'react';
import styles from './ClosetDetailModal.module.css';
import Button from '@/components/Button';
import Link from 'next/link';
import DeleteConfirmModal from '../DeleteConfirmModal/DeleteConfirmModal';
import FolderMoveModal from '../FolderMoveModal/FolderMoveModal';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getClosetItem, updateClosetItem, deleteClosetItem } from '@/lib/api/closetAPI';

const DUMMY_FOLDERS = [
  { id: 1, title: 'shirts outfit' },
  { id: 2, title: 'spring vibes' },
  { id: 3, title: 'wishlist' },
];

interface ClosetDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  itemId: string | null;
}

export default function ClosetDetailModal({ isOpen, onClose, itemId }: ClosetDetailModalProps) {
  const queryClient = useQueryClient();
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showMoveModal, setShowMoveModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editMemo, setEditMemo] = useState('');

  const { data: item, isLoading } = useQuery({
    queryKey: ['closetItem', itemId],
    queryFn: () => getClosetItem(itemId as string),
    enabled: !!itemId && isOpen,
  });

  const updateMutation = useMutation({
    mutationFn: (newMemo: string) => updateClosetItem(itemId as string, { memo: newMemo }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['closetItem', itemId] });
      setIsEditing(false);
    },
    onError: (error) => {
      console.error('Failed to update memo:', error);
      alert('메모 수정에 실패했습니다.');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: () => deleteClosetItem(itemId as string),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['closetItems'] });
      setShowDeleteConfirm(false);
      onClose();
    },
    onError: (error) => {
      console.error('Failed to delete item:', error);
      alert('아이템 삭제에 실패했습니다.');
    }
  });

  if (!isOpen || itemId === null) return null;

  // We only show the detail modal's internal content if move/delete modals aren't active
  const showMainContent = !showMoveModal && !showDeleteConfirm;

  const handleEditStart = () => {
    setEditMemo(item?.memo || '');
    setIsEditing(true);
  };

  const handleEditSave = () => {
    updateMutation.mutate(editMemo);
  };

  const handleEditCancel = () => {
    setIsEditing(false);
    setEditMemo('');
  };

  if (isLoading) {
    return (
      <div className={styles.overlay} onClick={onClose}>
        <div className={styles.modal}>
          <div className={styles.loading}>불러오는 중...</div>
        </div>
      </div>
    );
  }

  if (!item) return null;

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
                  <img src={item.imageUrl} alt="Item" className={styles.detailImage} />
                </div>

                <div className={styles.metaSection}>
                  {!isEditing ? (
                    <>
                      <button className={styles.editBtn} aria-label="메모 편집" onClick={handleEditStart}>
                        <img src="/icons/pencil.png" alt="Edit" className={styles.editIcon} />
                      </button>
                      <p className={styles.memo}>
                        {item.memo || '등록된 메모가 없습니다.'}
                      </p>
                    </>
                  ) : (
                    <div className={styles.editMode}>
                      <textarea
                        className={styles.memoInput}
                        value={editMemo}
                        onChange={(e) => setEditMemo(e.target.value)}
                        placeholder="메모를 입력하세요 (최대 100자)"
                        maxLength={100}
                        autoFocus
                      />
                      <div className={styles.editActions}>
                        <button className={styles.cancelBtn} onClick={handleEditCancel} disabled={updateMutation.isPending}>취소</button>
                        <button className={styles.saveBtn} onClick={handleEditSave} disabled={updateMutation.isPending}>
                          {updateMutation.isPending ? '저장 중...' : '저장'}
                        </button>
                      </div>
                    </div>
                  )}
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
          deleteMutation.mutate();
        }}
        onCancel={() => setShowDeleteConfirm(false)}
      />

      <FolderMoveModal
        isOpen={showMoveModal}
        itemIds={itemId ? [itemId] : []}
        currentFolderId={null} // Wait, we don't know the current folder here easily without item detail. We can pass null.
        variant="fullScreen"
        onSuccess={() => {
          setShowMoveModal(false);
          onClose();
        }}
        onCancel={() => setShowMoveModal(false)}
      />
    </>
  );
}
