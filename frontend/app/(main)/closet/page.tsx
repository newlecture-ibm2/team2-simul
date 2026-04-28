'use client';

import { useState } from 'react';
import ClosetCard from './_components/ClosetCard/ClosetCard';
import ClosetDetailModal from './_components/ClosetDetailModal/ClosetDetailModal';
import Toggle from './_components/Toggle/Toggle';
import styles from './page.module.css';

// Generate dummy clothing items
const DUMMY_ITEMS = Array.from({ length: 8 }, (_, i) => ({
  id: i + 1,
}));

export default function ClosetPage() {
  const [activeTab, setActiveTab] = useState<'Clothes' | 'Collections'>('Clothes');
  const [currentPage, setCurrentPage] = useState(1);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedItemId, setSelectedItemId] = useState<number | null>(null);

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
            { id: 'Clothes', icon: '/icons/hanger.png', label: '옷' },
            { id: 'Collections', icon: '/icons/square.split.bottomrightquarter.png', label: '컬렉션' }
          ]}
          activeId={activeTab}
          onChange={(id: string) => setActiveTab(id as 'Clothes' | 'Collections')}
        />
      </header>

      {/* Grid */}
      <div className={styles.grid}>
        {/* Upload Card */}
        <div className={styles.uploadCard}>
          <span className={styles.plusIcon}>+</span>
        </div>
        
        {DUMMY_ITEMS.slice(1).map(item => (
          <ClosetCard 
            key={item.id} 
            id={item.id} 
            onClick={handleCardClick}
          />
        ))}
      </div>

      {/* Pagination */}
      <div className={styles.paginationWrapper}>
        <div className={styles.paginationTrack}>
          {[1, 2, 3, 4, 5].map((page) => (
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

      <ClosetDetailModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        itemId={selectedItemId}
      />
    </div>
  );
}
