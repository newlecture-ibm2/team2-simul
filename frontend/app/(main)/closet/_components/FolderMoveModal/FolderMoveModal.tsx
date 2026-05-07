'use client';

import { useState } from 'react';
import styles from './FolderMoveModal.module.css';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getClosetCollections, updateItemCollection } from '@/lib/api/closetAPI';

interface FolderMoveModalProps {
  isOpen: boolean;
  itemIds: string[];
  currentFolderId?: string | null;
  onSuccess?: () => void;
  onCancel: () => void;
  variant?: 'popover' | 'fullScreen';
}

export default function FolderMoveModal({
  isOpen,
  itemIds,
  currentFolderId = null,
  onSuccess,
  onCancel,
  variant = 'popover',
}: FolderMoveModalProps) {
  const queryClient = useQueryClient();
  const [selectedId, setSelectedId] = useState<string | null>(
    currentFolderId ? String(currentFolderId) : null
  );

  const { data: collectionsData, isLoading } = useQuery({
    queryKey: ['closetCollections'],
    queryFn: () => getClosetCollections({ sort: 'recent', page: 0, size: 50 }),
    enabled: isOpen,
  });

  const moveMutation = useMutation({
    mutationFn: async (targetFolderId: string | null) => {
      // Move all itemIds to the targetFolderId
      const promises = itemIds.map(id => updateItemCollection(id, targetFolderId));
      await Promise.all(promises);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['closetItems'] });
      queryClient.invalidateQueries({ queryKey: ['closetCollections'] });
      onSuccess?.();
    },
    onError: (err) => {
      console.error('Failed to move items:', err);
      alert('이동에 실패했습니다.');
    }
  });

  if (!isOpen) return null;

  const folders = collectionsData?.collections || [];

  return (
    <div className={`${styles.overlay} ${variant === 'fullScreen' ? styles.fullScreenOverlay : ''}`} onClick={onCancel}>
      <div 
        className={`${styles.modal} ${variant === 'fullScreen' ? styles.fullScreenModal : ''}`} 
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className={styles.title}>이동할 폴더 선택</h2>
        
        <div className={styles.folderList}>
          {isLoading ? (
            <p className={styles.loadingText}>불러오는 중...</p>
          ) : (
            <>
              {/* 폴더 지정 안함 (기본 옷장) 옵션 */}
              <div
                className={`${styles.folderItem} ${selectedId === null ? styles.selected : ''}`}
                onClick={() => setSelectedId(null)}
              >
                <div className={styles.folderInfo}>
                  <img src="/icons/folder.png" alt="" className={styles.folderIcon} />
                  <span className={styles.folderName}>폴더 지정 안함 (기본)</span>
                </div>
                <div className={styles.radioCircle}>
                  <div className={styles.radioInner} />
                </div>
              </div>

              {/* 실제 폴더 목록 */}
              {folders.map((folder) => (
                <div
                  key={folder.collectionId}
                  className={`${styles.folderItem} ${selectedId === folder.collectionId ? styles.selected : ''}`}
                  onClick={() => setSelectedId(folder.collectionId)}
                >
                  <div className={styles.folderInfo}>
                    <img src="/icons/folder.png" alt="" className={styles.folderIcon} />
                    <span className={styles.folderName}>{folder.name}</span>
                  </div>
                  <div className={styles.radioCircle}>
                    <div className={styles.radioInner} />
                  </div>
                </div>
              ))}
            </>
          )}
        </div>

        <div className={styles.actions}>
          <button className={`${styles.btn} ${styles.cancelBtn}`} onClick={onCancel}>
            취소
          </button>
          <button
            className={`${styles.btn} ${styles.confirmBtn}`}
            onClick={() => moveMutation.mutate(selectedId)}
            disabled={moveMutation.isPending}
          >
            {moveMutation.isPending ? '이동 중...' : '이동'}
          </button>
        </div>
      </div>
    </div>
  );
}
