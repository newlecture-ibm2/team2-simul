'use client';

import { useState } from 'react';
import styles from './page.module.css';
import MainToggle from './_components/MainToggle';
import FeedGrid from './_components/FeedGrid';
import SwipeDeck from './_components/SwipeDeck/SwipeDeck';

export default function HomePage() {
  const [tab, setTab] = useState('all');

  return (
    <div className={styles.homePage}>
      <SwipeDeck />
      <MainToggle onTabChange={setTab} />
      <FeedGrid tab={tab} />
    </div>
  );
}
