'use client';

import { useState, useRef, useEffect } from 'react';
import { useRouter, useParams, useSearchParams } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { getClosetCollection, ClosetItemResponse } from '@/lib/api/closetAPI';
import { useClosetItems } from '../../_components/useClosetItems';
import ClosetCard from '../../_components/ClosetCard/ClosetCard';
import ClosetDetailModal from '../../_components/ClosetDetailModal/ClosetDetailModal';
import ConfirmModal from '../../_components/ConfirmModal/ConfirmModal';
import FolderMoveModal from '../../_components/FolderMoveModal/FolderMoveModal';
import AddItemModal from './_components/AddItemModal/AddItemModal';
import Toggle from '../../_components/Toggle/Toggle';
import FloatingAddButton from '../../_components/FloatingAddButton';
import ClosetAddModal from '../../_components/ClosetAddModal';
import AlertModal from '../../_components/AlertModal/AlertModal';
import styles from './page.module.css';

const ITEMS_PER_PAGE = 20;

export default function FolderDetailPage() {
  const router = useRouter();
  const params = useParams();
  const folderId = params.id as string;
  const searchParams = useSearchParams();
  const isEditInitial = searchParams.get('edit') === 'true';

  // 1. Fetch Collection Metadata
  const { data: collection, isLoading: isCollectionLoading } = useQuery({
    queryKey: ['closetCollection', folderId],
    queryFn: () => getClosetCollection(folderId),
  });

  const [currentPage, setCurrentPage] = useState(0); // Backend uses 0-indexed pages

  // 2. Fetch Collection Items
  const { 
    items: apiItems, 
    isLoading: isItemsLoading, 
    totalCount,
    refetch: refetchItems 
  } = useClosetItems({
    collectionId: folderId,
    page: currentPage,
    size: ITEMS_PER_PAGE,
    sort: 'recent'
  });

  const [viewMode, setViewMode] = useState<'view' | 'edit'>(isEditInitial ? 'edit' : 'view');
  const [folderTitle, setFolderTitle] = useState('');
  const [selectedItems, setSelectedItems] = useState<Set<string>>(new Set());
  const [items, setItems] = useState<ClosetItemResponse[]>([]); // For local manipulation/display
  const [dragIndex, setDragIndex] = useState<number | null>(null);
  const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showMoveModal, setShowMoveModal] = useState(false);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [selectedItemId, setSelectedItemId] = useState<string | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [isClosetAddModalOpen, setIsClosetAddModalOpen] = useState(false);
  const [showAlert, setShowAlert] = useState(false);

  // Sync title and items from API
  useEffect(() => {
    if (collection) setFolderTitle(collection.name);
  }, [collection]);

  useEffect(() => {
    setItems(apiItems);
  }, [apiItems]);

  const totalPages = Math.ceil(totalCount / ITEMS_PER_PAGE);

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
                if (!folderTitle.trim()) {
                  if (collection) setFolderTitle(collection.name);
                  setShowAlert(true);
                  return;
                }
                // TODO: updateClosetCollection API call
                console.log(`Renaming folder ${folderId} to ${folderTitle}`);
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter') (e.target as HTMLInputElement).blur();
              }}
              autoFocus
            />
          ) : (
            <h1 className={styles.title}>{isCollectionLoading ? '로딩 중...' : folderTitle}</h1>
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
      {(isCollectionLoading || isItemsLoading) ? (
        <div className={styles.loadingState}>아이템을 불러오는 중...</div>
      ) : items.length > 0 ? (
        <div className={styles.grid}>
          {items.map((item, index) => {
            const globalIndex = index; // Local index within the current page
            return (
              <div
                key={item.itemId}
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
                    className={`${styles.editOverlay} ${selectedItems.has(item.itemId) ? styles.selected : ''}`}
                    onClick={(e) => { e.stopPropagation(); toggleSelectItem(item.itemId); }}
                  >
                    {selectedItems.has(item.itemId) && (
                      <svg className={styles.checkIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    )}
                  </div>
                )}
                <ClosetCard id={item.itemId} imageUrl={item.imageUrl} onClick={handleCardClick} />
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
      <ConfirmModal
        isOpen={showDeleteModal}
        title="삭제하시겠습니까?"
        description={`선택한 ${selectedItems.size}개의 아이템이 영구적으로 삭제됩니다.`}
        confirmText="예"
        cancelText="아니오"
        isDestructive={true}
        onConfirm={() => {
          setItems(prev => prev.filter(item => !selectedItems.has(item.itemId)));
          setSelectedItems(new Set());
          setShowDeleteModal(false);
          refetchItems();
        }}
        onCancel={() => setShowDeleteModal(false)}
      />

      {/* Folder Move Modal */}
      <FolderMoveModal
        isOpen={showMoveModal}
        itemIds={Array.from(selectedItems)}
        currentFolderId={folderId}
        onSuccess={() => {
          setItems(prev => prev.filter(item => !selectedItems.has(item.itemId)));
          setSelectedItems(new Set());
          setShowMoveModal(false);
          refetchItems();
        }}
        onCancel={() => setShowMoveModal(false)}
      />

      <AddItemModal 
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        existingImageIds={items.map(item => item.imageId)}
        onAdd={async (selectedIds) => {
          try {
            const { copyItemsToCollection } = await import('@/lib/api/closetAPI');
            await copyItemsToCollection(selectedIds, folderId);
            setShowAddModal(false);
            refetchItems();
          } catch (error) {
            console.error('Failed to add items to folder:', error);
            alert('아이템 추가에 실패했습니다.');
          }
        }}
      />

      <ClosetDetailModal 
        isOpen={isDetailModalOpen} 
        onClose={() => setIsDetailModalOpen(false)} 
        itemId={selectedItemId}
      />

      <AlertModal
        isOpen={showAlert}
        title="폴더 이름 오류"
        message="폴더 이름은 최소 한 글자 이상이어야 합니다."
        onClose={() => setShowAlert(false)}
      />
    </div>
  );
}
