'use client';

import { useState } from 'react';
import styles from './AddItemModal.module.css';
import Button from '@/components/Button';
import ClosetCard from '../../../../_components/ClosetCard/ClosetCard';
import { useClosetItems } from '../../../../_components/useClosetItems';
import { useParams } from 'next/navigation';

interface AddItemModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAdd: (selectedIds: string[]) => void;
  existingImageIds: string[];
}

export default function AddItemModal({
  isOpen,
  onClose,
  onAdd,
  existingImageIds,
}: AddItemModalProps) {
  const params = useParams();
  const folderId = params.id as string;
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [currentPage, setCurrentPage] = useState(0);
  const ITEMS_PER_PAGE = 12;

  const { items: allItems, totalCount } = useClosetItems({
    page: currentPage,
    size: ITEMS_PER_PAGE,
    sort: 'recent'
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) return null;

  // 현재 폴더에 이미 저장된 아이템(이미지 기준) 제외
  const filteredItems = allItems.filter(item => !existingImageIds.includes(item.imageId!));
  const totalPages = Math.ceil(totalCount / ITEMS_PER_PAGE);

  const toggleItem = (id: string) => {
    const newSelected = new Set(selectedIds);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedIds(newSelected);
  };

  const handleConfirm = async () => {
    setIsSubmitting(true);
    try {
      await onAdd(Array.from(selectedIds));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2 className={styles.title}>아이템 추가</h2>
          <button className={styles.closeBtn} onClick={onClose}>✕</button>
        </div>

        <div className={styles.content}>
          {filteredItems.length > 0 ? (
            <div className={styles.grid}>
              {filteredItems.map((item) => (
                <div 
                  key={item.itemId} 
                  className={styles.cardWrapper}
                  onClick={() => toggleItem(item.itemId)}
                >
                  <ClosetCard id={item.itemId} imageUrl={item.imageUrl} />
                  <div className={`${styles.selectionOverlay} ${selectedIds.has(item.itemId) ? styles.selected : ''}`}>
                    <div className={styles.checkCircle}>
                      {selectedIds.has(item.itemId) && <span className={styles.checkIcon}>✓</span>}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className={styles.emptyState}>이동할 수 있는 아이템이 없습니다</div>
          )}
        </div>

        {totalPages > 1 && (
          <div className={styles.paginationWrapper}>
            <div className={styles.paginationTrack}>
              {Array.from({ length: totalPages }, (_, i) => i).map((page) => (
                <button
                  key={page}
                  className={`${styles.pageBtn} ${currentPage === page ? styles.activePage : ''}`}
                  onClick={() => setCurrentPage(page)}
                >
                  {page + 1}
                </button>
              ))}
            </div>
          </div>
        )}

        <div className={styles.actions}>
          <Button variant="secondary" size="lg" onClick={onClose} fullWidth disabled={isSubmitting}>
            취소
          </Button>
          <Button 
            variant="primary" 
            size="lg" 
            onClick={handleConfirm}
            fullWidth
            disabled={selectedIds.size === 0 || isSubmitting}
          >
            {isSubmitting ? '처리 중...' : `확인 (${selectedIds.size})`}
          </Button>
        </div>
      </div>
    </div>
  );
}
