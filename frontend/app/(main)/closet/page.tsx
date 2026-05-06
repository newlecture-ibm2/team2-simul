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
import { useMutation, useQueryClient, useQuery } from '@tanstack/react-query';
import { addClosetItem, addClosetCollection, getClosetCollections } from '@/lib/api/closetAPI';

// const DUMMY_FOLDERS_DATA = [
//   { id: 1, title: 'shirts outfit', itemCount: 3, lastUpdated: '2주 전', images: [] },
//   { id: 2, title: 'spring vibes', itemCount: 8, lastUpdated: '1달 전', images: [] },
//   { id: 3, title: 'wishlist', itemCount: 12, lastUpdated: '어제', images: [] },
// ];

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
  const [editingFolderId, setEditingFolderId] = useState<string | null>(null);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  // API 호출: 옷장 아이템 목록
  const apiParams = useMemo(() => ({
    page: currentPage,
    size: PAGE_SIZE,
    sort: 'recent',
  }), [currentPage]);

  const { items, isLoading, error, totalCount, refetch } = useClosetItems(apiParams);

  // API 호출: 컬렉션 목록
  const { data: collectionsData, isLoading: isLoadingCollections } = useQuery({
    queryKey: ['closetCollections'],
    queryFn: () => getClosetCollections({ sort: 'recent', page: 0, size: 50 }),
  });

  const displayCollections = useMemo(() => {
    if (!collectionsData) return [];
    return collectionsData.collections.map(c => ({
      id: c.collectionId,
      title: c.name,
      itemCount: c.itemCount,
      lastUpdated: new Date(c.createdAt).toLocaleDateString(),
      images: c.coverImageUrl ? [c.coverImageUrl] : []
    }));
  }, [collectionsData]);

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

  const addCollectionMutation = useMutation({
    mutationFn: (formData: FormData) => addClosetCollection(formData),
    onSuccess: (data: string) => {
      const collectionId = data;
      console.log('Collection created with ID:', collectionId);
      queryClient.invalidateQueries({ queryKey: ['closetCollections'] });
      if (collectionId) setEditingFolderId(collectionId);
    },
    onError: (error) => {
      console.error('Failed to create collection:', error);
      alert('폴더 생성에 실패했습니다.');
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
    // 1. API 호출: 기본 이름 '새 폴더'로 생성
    const formData = new FormData();
    formData.append('name', '새 폴더');
    addCollectionMutation.mutate(formData);
  };

  const handleRenameFolder = (_id: string, _newTitle: string) => {
    // TODO: 폴더 이름 변경 PATCH API 호출 필요
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

      {/* Main Content */}
      <div className={styles.content}>
        {/* Stack Tab */}
        {activeTab === 'stack' && (
          <>
            {isLoading ? (
              <div className={styles.loadingWrapper}><p>아이템을 불러오는 중...</p></div>
            ) : error ? (
              <div className={styles.errorWrapper}>
                <p>⚠️ {error}</p>
                <button onClick={refetch}>다시 시도</button>
              </div>
            ) : (
              <VerticalDeck 
                items={displayItems} 
                onItemClick={handleCardClick} 
              />
            )}
          </>
        )}

        {/* Grid Tab */}
        {activeTab === 'grid' && (
          <>
            {isLoading ? (
              <div className={styles.loadingWrapper}><p>아이템을 불러오는 중...</p></div>
            ) : error ? (
              <div className={styles.errorWrapper}>
                <p>⚠️ {error}</p>
                <button onClick={refetch}>다시 시도</button>
              </div>
            ) : (
              <>
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
          </>
        )}
        
        {/* Folder Tab */}
        {activeTab === 'folder' && (
            <>
              {isLoadingCollections ? (
                <div className={styles.loadingWrapper}><p>폴더를 불러오는 중...</p></div>
              ) : (
                <div className={styles.folderGrid}>
                  {displayCollections.map(folder => (
                    <FolderCard 
                      key={folder.id}
                      id={folder.id}
                      title={folder.title}
                      itemCount={folder.itemCount}
                      lastUpdated={folder.lastUpdated}
                      images={folder.images}
                      onClick={(id) => router.push(`/closet/folders/${id}`)}
                      isEditing={editingFolderId === folder.id}
                      onRename={(id, title) => handleRenameFolder(String(id), title)}
                    />
                  ))}
                </div>
              )}

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
        </div>

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
