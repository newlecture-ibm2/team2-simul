'use client';

import { useState } from 'react';
import styles from './MainToggle.module.css';

interface MainToggleProps {
  onTabChange?: (tab: string) => void;
}

export default function MainToggle({ onTabChange }: MainToggleProps) {
  const [active, setActive] = useState('전체');
  const tabs = ['전체', '팔로잉'];

  const handleTabClick = (tab: string) => {
    setActive(tab);
    onTabChange?.(tab === '전체' ? 'all' : 'following');
  };

  return (
    <div className={styles.toggleContainer}>
      <div className={styles.toggleTrack}>
        {tabs.map((tab) => (
          <button
            key={tab}
            className={`${styles.toggleItem} ${active === tab ? styles.toggleItemActive : ''}`}
            onClick={() => handleTabClick(tab)}
          >
            {tab}
          </button>
        ))}
      </div>
    </div>
  );
}
