'use client';

import { useState } from 'react';
import styles from './page.module.css';
import MainToggle from './_components/MainToggle';
import FeedGrid from './_components/FeedGrid';

const SortIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <path d="M7 15l5 5 5-5"/>
    <path d="M7 9l5-5 5 5"/>
  </svg>
);

export default function CommunityPage() {
  const [tab, setTab] = useState('all');
  const [sort, setSort] = useState('recent'); // 'recent' | 'popular'

  const toggleSort = () => {
    setSort(prev => prev === 'recent' ? 'popular' : 'recent');
  };

  return (
    <div className={styles.pageContainer}>
      <div className={styles.headerControls}>
        <MainToggle onTabChange={setTab} />
        <button className={styles.sortToggle} onClick={toggleSort}>
          {sort === 'recent' ? '최신순' : '인기순'}
          <span className={styles.sortIcon}>
            <SortIcon />
          </span>
        </button>
      </div>
      <FeedGrid tab={tab} sort={sort} />
    </div>
  );
}
