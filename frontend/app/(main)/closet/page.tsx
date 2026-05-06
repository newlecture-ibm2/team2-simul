'use client';

import { useState, Suspense, useMemo } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import ClosetCard from './_components/ClosetCard/ClosetCard';
import ClosetDetailModal from './_components/ClosetDetailModal/ClosetDetailModal';
import FolderCard from './_components/FolderCard/FolderCard';
import Toggle from './_components/Toggle/Toggle';
import VerticalDeck from './_components/VerticalDeck/VerticalDeck';
import FloatingAddButton from './_components/FloatingAddButton';
import ClosetAddModal from './_components/ClosetAddModal/ClosetAddModal';
import { useClosetItems } from './_components/useClosetItems';
import styles from './page.module.css';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addClosetItem } from '@/lib/api/closetAPI';

const DUMMY_FOLDERS_DATA = [
  { id: 1, title: 'shirts outfit', itemCount: 3, lastUpdated: '2주 전', images: [] },
  { id: 2, title: 'spring vibes', itemCount: 8, lastUpdated: '1달 전', images: [] },
  { id: 3, title: 'wishlist', itemCount: 12, lastUpdated: '어제', images: [] },
];

const PAGE_SIZE = 10;

function ClosetPageContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const tabParam = searchParams.get('tab') as 'stack' | 'grid' | 'folder' | null;
  const queryClient = useQueryClient();
  
  const [activeTab, setActiveTab] = useState<'stack' | 'grid' | 'folder'>(tabParam || 'stack');
  const [currentPage, setCurrentPage] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedItemId, setSelectedItemId] = useState<string | null>(null);
  const [folders, setFolders] = useState(DUMMY_FOLDERS_DATA);
  const [editingFolderId, setEditingFolderId] = useState<number | null>(null);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  // API 호출: 옷장 아이템 목록
  const apiParams = useMemo(() => ({
    page: currentPage,
    size: PAGE_SIZE,
    sort: 'recent',
  }), [currentPage]);

  const { items, isLoading, error, totalCount, refetch } = useClosetItems(apiParams);

  const addMutation = useMutation({
    mutationFn: (formData: FormData) => addClosetItem(formData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['closetItems'] });
      setIsAddModalOpen(false);
    },
    onError: (error) => {
      console.error('Failed to add item:', error);
      alert('아이템 추가에 실패했습니다.');
    }
  });

  // VerticalDeck / Grid용 아이템 변환
  const displayItems = useMemo(() =>
    items.map(item => ({
      id: item.itemId,
      imageUrl: item.imageUrl,
      category: item.category,
      memo: item.memo,
    })),
    [items]
  );

  const totalPages = Math.ceil(totalCount / PAGE_SIZE);

  const handleAddFolder = () => {
    const newId = Date.now();
    const newFolder = {
      id: newId,
      title: '새 폴더',
      itemCount: 0,
      lastUpdated: '방금 전',
      images: []
    };
    setFolders([...folders, newFolder]);
    setEditingFolderId(newId);
  };

  const handleRenameFolder = (id: number, newTitle: string) => {
    setFolders(prev => prev.map(f => f.id === id ? { ...f, title: newTitle || '제목 없음' } : f));
    setEditingFolderId(null);
  };

  const handleCardClick = (id: string) => {
    setSelectedItemId(id);
    setIsModalOpen(true);
  };

  return (
    <div className={styles.closetPage}>
      {/* Header & Toggle */}
      <header className={styles.header}>
        <h1 className={styles.title}>나의 옷장</h1>
        
        <Toggle 
          options={[
            { id: 'stack', icon: '/icons/hanger.png', label: '세로로보기' },
            { id: 'grid', icon: '/icons/rectangle.grid.2x2.png', label: '그리드로보기' },
            { id: 'folder', icon: '/icons/folder.png', label: '폴더로 보기' }
          ]}
          activeId={activeTab}
          onChange={(id: string) => setActiveTab(id as 'stack' | 'grid' | 'folder')}
        />
      </header>

      {/* Loading & Error States */}
      {isLoading && (
        <div className={styles.loadingWrapper}>
          <p>아이템을 불러오는 중...</p>
        </div>
      )}

      {error && (
        <div className={styles.errorWrapper}>
          <p>⚠️ {error}</p>
          <button onClick={refetch}>다시 시도</button>
        </div>
      )}

      {/* Main Content */}
      {!isLoading && !error && (
        <>
          {activeTab === 'stack' && (
            <VerticalDeck 
              items={displayItems} 
              onItemClick={handleCardClick} 
            />
          )}
          {activeTab === 'grid' && (
            <>
              {/* Grid */}
              <div className={styles.grid}>
                {displayItems.map(item => (
                  <ClosetCard 
                    key={item.id} 
                    id={item.id} 
                    imageUrl={item.imageUrl}
                    onClick={handleCardClick}
                  />
                ))}
              </div>

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
            </>
          )}
          
          {activeTab === 'folder' && (
            <>
              <div className={styles.folderGrid}>
                {folders.map(folder => (
                  <FolderCard 
                    key={folder.id}
                    id={folder.id}
                    title={folder.title}
                    itemCount={folder.itemCount}
                    lastUpdated={folder.lastUpdated}
                    images={folder.images}
                    onClick={(id) => router.push(`/closet/folders/${id}`)}
                    isEditing={editingFolderId === folder.id}
                    onRename={handleRenameFolder}
                  />
                ))}
              </div>

              {/* Bottom Add Button (Folder Tab End) */}
              <div className={styles.bottomAddBtnWrapper}>
                <button 
                  className={styles.gridAddBtn} 
                  aria-label="폴더 추가"
                  onClick={handleAddFolder}
                >
                  <span className={styles.plusIconLarge}>+</span>
                </button>
              </div>
            </>
          )}
        </>
      )}

      {/* Floating Add Button */}
      <FloatingAddButton 
        ariaLabel="아이템 추가" 
        onClick={() => setIsAddModalOpen(true)} 
      />

      <ClosetAddModal 
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSave={(formData) => {
          addMutation.mutate(formData);
        }}
      />

      <ClosetDetailModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        itemId={selectedItemId}
      />
    </div>
  );
}

export default function ClosetPage() {
  return (
    <Suspense fallback={<div className={styles.loadingWrapper}>Loading...</div>}>
      <ClosetPageContent />
    </Suspense>
  );
}
