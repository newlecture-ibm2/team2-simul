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
        <div className={styles.sortSelectWrapper}>
          <select 
            className={styles.sortSelect} 
            value={sort} 
            onChange={(e) => setSort(e.target.value)}
          >
            <option value="recent">최신순</option>
            <option value="popular">인기순</option>
          </select>
          <svg className={styles.sortArrow} width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="6 9 12 15 18 9"></polyline>
          </svg>
        </div>
      </div>
      <FeedGrid tab={tab} sort={sort} />
    </div>
  );
}
