'use client';

import { useState } from 'react';
import styles from './page.module.css';
import MainToggle from './_components/MainToggle';
import FeedGrid from './_components/FeedGrid';

const RecentIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10" />
    <polyline points="12 6 12 12 15 15" />
  </svg>
);

const PopularIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/>
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
          <span className={styles.sortIcon}>
            {sort === 'recent' ? <RecentIcon /> : <PopularIcon />}
          </span>
          {sort === 'recent' ? '최신순' : '인기순'}
        </button>
      </div>
      <FeedGrid tab={tab} sort={sort} />
    </div>
  );
}
