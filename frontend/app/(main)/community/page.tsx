'use client';

import { useState } from 'react';
import styles from './page.module.css';
import MainToggle from './_components/MainToggle';
import FeedGrid from './_components/FeedGrid';

export default function CommunityPage() {
  const [tab, setTab] = useState('all');

  return (
    <div className={styles.pageContainer}>
      <MainToggle onTabChange={setTab} />
      <FeedGrid tab={tab} />
    </div>
  );
}
