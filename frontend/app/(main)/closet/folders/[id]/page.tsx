'use client';

import { useState, useRef } from 'react';
import { useRouter, useParams } from 'next/navigation';
import ClosetCard from '../../_components/ClosetCard/ClosetCard';
import ClosetDetailModal from '../../_components/ClosetDetailModal/ClosetDetailModal';
import DeleteConfirmModal from '../../_components/DeleteConfirmModal/DeleteConfirmModal';
import FolderMoveModal from './_components/FolderMoveModal/FolderMoveModal';
import AddItemModal from './_components/AddItemModal/AddItemModal';
import Toggle from '../../_components/Toggle/Toggle';
import FloatingAddButton from '../../_components/FloatingAddButton';
import ClosetAddModal from '../../_components/ClosetAddModal';
import styles from './page.module.css';

// Dummy folder metadata (would come from API)
const FOLDER_DATA: Record<string, { title: string; items: { id: string }[] }> = {
  '1': { title: 'shirts outfit', items: Array.from({ length: 3 }, (_, i) => ({ id: String(i + 1) })) },
  '2': { title: 'spring vibes', items: Array.from({ length: 8 }, (_, i) => ({ id: String(i + 1) })) },
  '3': { title: 'wishlist', items: Array.from({ length: 12 }, (_, i) => ({ id: String(i + 1) })) },
};

const ITEMS_PER_PAGE = 10;

export default function FolderDetailPage() {
  const router = useRouter();
  const params = useParams();
  const folderId = params.id as string;

  const folderMeta = FOLDER_DATA[folderId] || { title: '폴더', items: [] };

  const [viewMode, setViewMode] = useState<'view' | 'edit'>('view');
  const [folderTitle, setFolderTitle] = useState(folderMeta.title);
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedItems, setSelectedItems] = useState<Set<string>>(new Set());
  const [items, setItems] = useState(folderMeta.items);
  const [dragIndex, setDragIndex] = useState<number | null>(null);
  const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showMoveModal, setShowMoveModal] = useState(false);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [selectedItemId, setSelectedItemId] = useState<string | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [isClosetAddModalOpen, setIsClosetAddModalOpen] = useState(false);

  // Available items to add (dummy data)
  const AVAILABLE_ITEMS = Array.from({ length: 20 }, (_, i) => ({ id: String(i + 100) }));

  // For the modal list, we'll convert FOLDER_DATA to an array
  const allFolders = Object.entries(FOLDER_DATA).map(([id, data]) => ({ id, title: data.title }));

  const totalPages = Math.ceil(items.length / ITEMS_PER_PAGE);
  const currentItems = items.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  const toggleSelectItem = (id: string) => {
    setSelectedItems(prev => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const handleCardClick = (id: string) => {
    if (viewMode === 'edit') {
      toggleSelectItem(id);
    } else {
      setSelectedItemId(id);
      setIsDetailModalOpen(true);
    }
  };

  // Drag & Drop handlers (edit mode only) — live reorder
  const dragIndexRef = useRef<number | null>(null);

  const handleDragStart = (index: number) => {
    dragIndexRef.current = index;
    setDragIndex(index);
  };

  const handleDragOver = (e: React.DragEvent, overIndex: number) => {
    e.preventDefault();
    const from = dragIndexRef.current;
    if (from === null || from === overIndex) return;

    // Live reorder: move the item immediately
    setItems(prev => {
      const reordered = [...prev];
      const [moved] = reordered.splice(from, 1);
      reordered.splice(overIndex, 0, moved);
      return reordered;
    });

    // Update the tracked index to the new position
    dragIndexRef.current = overIndex;
    setDragIndex(overIndex);
    setDragOverIndex(null);
  };

  const handleDragLeave = () => {
    setDragOverIndex(null);
  };

  const handleDrop = () => {
    dragIndexRef.current = null;
    setDragIndex(null);
    setDragOverIndex(null);
  };

  const handleDragEnd = () => {
    dragIndexRef.current = null;
    setDragIndex(null);
    setDragOverIndex(null);
  };

  return (
    <div className={styles.folderDetailPage}>
      {/* Header */}
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <button className={styles.backBtn} onClick={() => router.push('/closet?tab=folder')} aria-label="뒤로가기">
            <img src="/icons/arrow-left.png" alt="뒤로가기" className={styles.backIcon} />
          </button>
          {viewMode === 'edit' ? (
            <input
              className={styles.titleInput}
              value={folderTitle}
              onChange={(e) => setFolderTitle(e.target.value)}
              onBlur={() => {
                if (!folderTitle.trim()) setFolderTitle(folderMeta.title);
                console.log(`Renaming folder ${folderId} to ${folderTitle}`);
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter') (e.target as HTMLInputElement).blur();
              }}
              autoFocus
            />
          ) : (
            <h1 className={styles.title}>{folderTitle}</h1>
          )}
        </div>

        <Toggle
          options={[
            { id: 'view', icon: '/icons/eyes.png', label: '보기' },
            { id: 'edit', icon: '/icons/pencil.png', label: '수정하기' },
          ]}
          activeId={viewMode}
          size="small"
          onChange={(id: string) => {
            setViewMode(id as 'view' | 'edit');
            if (id === 'view') setSelectedItems(new Set());
          }}
        />
      </header>

      {/* Grid */}
      {items.length > 0 ? (
        <div className={styles.grid}>
          {currentItems.map((item, index) => {
            const globalIndex = (currentPage - 1) * ITEMS_PER_PAGE + index;
            return (
              <div
                key={item.id}
                className={`${styles.cardWrapper} ${dragIndex === globalIndex ? styles.dragging : ''} ${dragOverIndex === globalIndex ? styles.dragOver : ''}`}
                draggable={viewMode === 'edit'}
                onDragStart={() => viewMode === 'edit' && handleDragStart(globalIndex)}
                onDragOver={(e) => viewMode === 'edit' && handleDragOver(e, globalIndex)}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                onDragEnd={handleDragEnd}
              >
                {viewMode === 'edit' && (
                  <div
                    className={`${styles.editOverlay} ${selectedItems.has(item.id) ? styles.selected : ''}`}
                    onClick={(e) => { e.stopPropagation(); toggleSelectItem(item.id); }}
                  >
                    {selectedItems.has(item.id) && (
                      <svg className={styles.checkIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    )}
                  </div>
                )}
                <ClosetCard id={item.id} onClick={handleCardClick} />
              </div>
            );
          })}
        </div>
      ) : (
        <div className={styles.emptyState}>
          <p>이 폴더에 아이템이 없습니다</p>
        </div>
      )}

      {/* Pagination */}
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

      {/* Floating Add Button (Always exists) */}
      <FloatingAddButton 
        ariaLabel="아이템 직접 추가" 
        onClick={() => setIsClosetAddModalOpen(true)}
      />

      <ClosetAddModal 
        isOpen={isClosetAddModalOpen}
        onClose={() => setIsClosetAddModalOpen(false)}
        onSave={(data) => {
          console.log('Saving new item from folder page:', data);
          // TODO: API integration
        }}
      />

      {/* Edit Mode - Floating Action Buttons (below + button) */}
      {viewMode === 'edit' && (
        <div className={styles.floatingActionGroup}>
          <button
            className={`${styles.floatingActionBtn} ${styles.addBtn}`}
            aria-label="폴더에 아이템 추가"
            onClick={() => setShowAddModal(true)}
          >
            <img src="/icons/folder.badge.plus.png" alt="추가" className={styles.actionIcon} />
          </button>
          
          {selectedItems.size > 0 && (
            <>
              <button
                className={`${styles.floatingActionBtn} ${styles.moveBtn}`}
                aria-label="이동"
                onClick={() => setShowMoveModal(true)}
              >
                <img src="/icons/tray.and.arrow.up.png" alt="이동" className={styles.actionIcon} />
              </button>
              <button
                className={`${styles.floatingActionBtn} ${styles.deleteBtn}`}
                aria-label="삭제"
                onClick={() => setShowDeleteModal(true)}
              >
                <img src="/icons/trash.png" alt="삭제" className={styles.actionIcon} />
              </button>
            </>
          )}
        </div>
      )}

      {/* Delete Confirm Modal */}
      <DeleteConfirmModal
        isOpen={showDeleteModal}
        count={selectedItems.size}
        onConfirm={() => {
          setItems(prev => prev.filter(item => !selectedItems.has(item.id)));
          setSelectedItems(new Set());
          setShowDeleteModal(false);
        }}
        onCancel={() => setShowDeleteModal(false)}
      />

      {/* Folder Move Modal */}
      <FolderMoveModal
        isOpen={showMoveModal}
        folders={allFolders}
        currentFolderId={folderId}
        onConfirm={(targetId) => {
          console.log(`Moving ${selectedItems.size} items to folder ${targetId}`);
          setItems(prev => prev.filter(item => !selectedItems.has(item.id)));
          setSelectedItems(new Set());
          setShowMoveModal(false);
        }}
        onCancel={() => setShowMoveModal(false)}
      />

      <AddItemModal 
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        availableItems={AVAILABLE_ITEMS}
        onAdd={(selectedIds) => {
          const newItems = selectedIds.map(id => ({ id }));
          setItems(prev => [...prev, ...newItems]);
          setShowAddModal(false);
        }}
      />

      <ClosetDetailModal 
        isOpen={isDetailModalOpen} 
        onClose={() => setIsDetailModalOpen(false)} 
        itemId={selectedItemId}
      />
    </div>
  );
}
