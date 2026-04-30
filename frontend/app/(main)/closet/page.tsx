'use client';

import { useState, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import ClosetCard from './_components/ClosetCard/ClosetCard';
import ClosetDetailModal from './_components/ClosetDetailModal/ClosetDetailModal';
import FolderCard from './_components/FolderCard/FolderCard';
import Toggle from './_components/Toggle/Toggle';
import VerticalDeck from './_components/VerticalDeck/VerticalDeck';
import FloatingAddButton from './_components/FloatingAddButton';
import ClosetAddModal from './_components/ClosetAddModal';
import styles from './page.module.css';

// Generate dummy clothing items
const DUMMY_ITEMS = Array.from({ length: 20 }, (_, i) => ({
  id: i + 1,
}));

const DUMMY_FOLDERS_DATA = [
  { id: 1, title: 'shirts outfit', itemCount: 3, lastUpdated: '2주 전', images: [] },
  { id: 2, title: 'spring vibes', itemCount: 8, lastUpdated: '1달 전', images: [] },
  { id: 3, title: 'wishlist', itemCount: 12, lastUpdated: '어제', images: [] },
];

function ClosetContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const tabParam = searchParams.get('tab') as 'stack' | 'grid' | 'folder' | null;
  
  const [activeTab, setActiveTab] = useState<'stack' | 'grid' | 'folder'>(tabParam || 'stack');
  const [currentPage, setCurrentPage] = useState(1);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedItemId, setSelectedItemId] = useState<number | null>(null);
  const [folders, setFolders] = useState(DUMMY_FOLDERS_DATA);
  const [editingFolderId, setEditingFolderId] = useState<number | null>(null);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

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

  const handleCardClick = (id: number) => {
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
      {activeTab === 'stack' && (
        <VerticalDeck 
          items={DUMMY_ITEMS} 
          onItemClick={handleCardClick} 
        />
      )}
      {activeTab === 'grid' && (
        <>
          {/* Grid */}
          <div className={styles.grid}>
            {DUMMY_ITEMS.slice((currentPage - 1) * 10, currentPage * 10).map(item => (
              <ClosetCard 
                key={item.id} 
                id={item.id} 
                onClick={handleCardClick}
              />
            ))}
          </div>

          {/* Pagination */}
          {Math.ceil(DUMMY_ITEMS.length / 10) > 1 && (
            <div className={styles.paginationWrapper}>
              <div className={styles.paginationTrack}>
                {Array.from({ length: Math.ceil(DUMMY_ITEMS.length / 10) }, (_, i) => i + 1).map((page) => (
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

      {/* Floating Add Button */}
      <FloatingAddButton 
        ariaLabel="아이템 추가" 
        onClick={() => setIsAddModalOpen(true)} 
      />

      <ClosetAddModal 
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSave={(data) => {
          console.log('Saving new item:', data);
          // TODO: API integration
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
    <Suspense fallback={null}>
      <ClosetContent />
    </Suspense>
  );
}
