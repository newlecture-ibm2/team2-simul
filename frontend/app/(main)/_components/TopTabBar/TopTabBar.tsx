'use client';

import { useState } from 'react';
import styles from './TopTabBar.module.css';

interface TopTabBarProps {
  tabs: string[];
  defaultIndex?: number;
  onChange?: (index: number) => void;
}

export default function TopTabBar({
  tabs,
  defaultIndex = 0,
  onChange,
}: TopTabBarProps) {
  const [active, setActive] = useState(defaultIndex);

  const handleClick = (index: number) => {
    setActive(index);
    onChange?.(index);
  };

  return (
    <div className={styles.tabBar} role="tablist">
      {tabs.map((tab, i) => (
        <button
          key={tab}
          role="tab"
          aria-selected={i === active}
          className={`${styles.tab} ${i === active ? styles.tabActive : ''}`}
          onClick={() => handleClick(i)}
        >
          {tab}
        </button>
      ))}
    </div>
  );
}
