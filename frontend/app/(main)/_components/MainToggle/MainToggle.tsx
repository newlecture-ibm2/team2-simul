'use client';

import { useState } from 'react';
import styles from './MainToggle.module.css';

export default function MainToggle() {
  const [active, setActive] = useState('전체');
  const tabs = ['전체', '팔로잉'];

  return (
    <div className={styles.toggleContainer}>
      <div className={styles.toggleTrack}>
        {tabs.map((tab) => (
          <button
            key={tab}
            className={`${styles.toggleItem} ${active === tab ? styles.toggleItemActive : ''}`}
            onClick={() => setActive(tab)}
          >
            {tab}
          </button>
        ))}
      </div>
    </div>
  );
}
