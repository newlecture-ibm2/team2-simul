'use client';

import { useState, useEffect, useRef } from 'react';
import styles from './page.module.css';
import MainToggle from './_components/MainToggle';
import FeedGrid from './_components/FeedGrid';



export default function CommunityPage() {
  const [tab, setTab] = useState('all');
  const [sort, setSort] = useState('recent');
  const [isSortOpen, setIsSortOpen] = useState(false);
  const sortRef = useRef<HTMLDivElement>(null);

  // 외부 클릭 시 정렬 메뉴 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (sortRef.current && !sortRef.current.contains(event.target as Node)) {
        setIsSortOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const toggleSort = () => {
    setSort(prev => prev === 'recent' ? 'popular' : 'recent');
  };

  return (
    <div className={styles.pageContainer}>
      <div className={`${styles.headerControls} ${isSortOpen ? styles.sortOpen : ''}`}>
        <MainToggle onTabChange={setTab} />
        <div className={styles.sortDropdownContainer} ref={sortRef}>
          <button 
            className={styles.sortButton} 
            onClick={() => setIsSortOpen(!isSortOpen)}
          >
            {sort === 'recent' ? '최신순' : '인기순'}
            <svg 
              className={`${styles.sortArrow} ${isSortOpen ? styles.sortArrowOpen : ''}`} 
              width="14" height="14" viewBox="0 0 24 24" fill="none" 
              stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"
            >
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </button>

          {isSortOpen && (
            <>
              {/* Invisible overlay for catching outside clicks to close the popover */}
              <div className={styles.mobileOverlay} onClick={() => setIsSortOpen(false)} />
              
              <div className={styles.sortMenu}>
                <button 
                  className={`${styles.sortMenuItem} ${sort === 'recent' ? styles.active : ''}`}
                  onClick={() => { setSort('recent'); setIsSortOpen(false); }}
                >
                  최신순
                </button>
                <button 
                  className={`${styles.sortMenuItem} ${sort === 'popular' ? styles.active : ''}`}
                  onClick={() => { setSort('popular'); setIsSortOpen(false); }}
                >
                  인기순
                </button>
              </div>
            </>
          )}
        </div>
      </div>
      <FeedGrid tab={tab} sort={sort} />
    </div>
  );
}
