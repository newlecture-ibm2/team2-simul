'use client';

import { useState } from 'react';
import styles from './page.module.css';
import MainToggle from './_components/MainToggle';
import FeedGrid from './_components/FeedGrid';

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
          <span className={styles.sortIcon}>🔄</span>
          {sort === 'recent' ? '최신순' : '인기순'}
        </button>
      </div>
      <FeedGrid tab={tab} sort={sort} />
    </div>
  );
}
