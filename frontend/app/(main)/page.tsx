'use client';

import styles from './page.module.css';
import SwipeDeck from './_components/SwipeDeck/SwipeDeck';

export default function HomePage() {
  return (
    <div className={styles.homePage}>
      <SwipeDeck />
    </div>
  );
}
