'use client';

import { useState } from 'react';
import styles from './AddItemModal.module.css';
import Button from '@/components/Button';
import ClosetCard from '../../../../_components/ClosetCard/ClosetCard';

interface Item {
  id: string;
  imageUrl?: string;
}

interface AddItemModalProps {
  isOpen: boolean;
  onClose: () => void;
  availableItems: Item[];
  onAdd: (selectedIds: string[]) => void;
}

export default function AddItemModal({
  isOpen,
  onClose,
  availableItems,
  onAdd,
}: AddItemModalProps) {
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 9;

  if (!isOpen) return null;

  const totalPages = Math.ceil(availableItems.length / ITEMS_PER_PAGE);
  const currentItems = availableItems.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  const toggleItem = (id: string) => {
    const newSelected = new Set(selectedIds);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedIds(newSelected);
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2 className={styles.title}>아이템 추가</h2>
          <button className={styles.closeBtn} onClick={onClose}>✕</button>
        </div>

        <div className={styles.content}>
          <div className={styles.grid}>
            {currentItems.map((item) => (
              <div 
                key={item.id} 
                className={styles.cardWrapper}
                onClick={() => toggleItem(item.id)}
              >
                <ClosetCard id={item.id} />
                <div className={`${styles.selectionOverlay} ${selectedIds.has(item.id) ? styles.selected : ''}`}>
                  <div className={styles.checkCircle}>
                    {selectedIds.has(item.id) && <span className={styles.checkIcon}>✓</span>}
                  </div>
                </div>
              </div>
            ))}
          </div>

        </div>

        {totalPages > 1 && (
          <div className={styles.paginationWrapper}>
            <div className={styles.paginationTrack}>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                <button
                  key={page}
                  className={`${styles.pageBtn} ${currentPage === page ? styles.activePage : ''}`}
                  onClick={() => setCurrentPage(page)}
                >
                  {page}
                </button>
              ))}
            </div>
          </div>
        )}

        <div className={styles.actions}>
          <Button variant="secondary" size="lg" onClick={onClose} fullWidth>
            취소
          </Button>
          <Button 
            variant="primary" 
            size="lg" 
            onClick={() => onAdd(Array.from(selectedIds))}
            fullWidth
            disabled={selectedIds.size === 0}
          >
            확인 ({selectedIds.size})
          </Button>
        </div>
      </div>
    </div>
  );
}
