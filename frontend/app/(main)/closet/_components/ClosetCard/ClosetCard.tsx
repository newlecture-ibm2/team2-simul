'use client';

import { useState } from 'react';
import styles from './ClosetCard.module.css';

interface ClosetCardProps {
  id: string;
  imageUrl?: string;
  onClick?: (id: string) => void;
}

export default function ClosetCard({ 
  id, 
  imageUrl,
  onClick
}: ClosetCardProps) {
  const displayImage = imageUrl || '/clothes.png';

  return (
    <div className={styles.card} onClick={() => onClick?.(id)}>
      <div className={styles.imageWrapper}>
        <img src={displayImage} alt="옷장 아이템" className={styles.image} />
      </div>
    </div>
  );
}
