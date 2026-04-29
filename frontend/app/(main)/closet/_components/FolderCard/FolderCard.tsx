'use client';

import { useState } from 'react';
import styles from './FolderCard.module.css';

interface FolderCardProps {
  id: number;
  title: string;
  itemCount: number;
  lastUpdated: string;
  images: string[];
  onClick?: (id: number) => void;
  isEditing?: boolean;
  onRename?: (id: number, newTitle: string) => void;
}

export default function FolderCard({
  id,
  title,
  itemCount,
  lastUpdated,
  images,
  onClick,
  isEditing = false,
  onRename
}: FolderCardProps) {
  // Use dummy fallbacks if images are missing
  const mainImage = images[0] || '/clothes.png';
  const subImage1 = images[1] || '/clothes.png';
  const subImage2 = images[2] || '/clothes.png';

  const [tempTitle, setTempTitle] = useState(title);

  return (
    <div className={styles.folderCard} onClick={() => !isEditing && onClick?.(id)}>
      <div className={styles.imageGrid}>
        <div className={styles.mainImageWrapper}>
          <img src={mainImage} alt={`${title} 메인`} className={styles.image} />
        </div>
        <div className={styles.subImagesWrapper}>
          <div className={styles.subImageWrapper}>
            <img src={subImage1} alt={`${title} 서브1`} className={styles.image} />
          </div>
          <div className={styles.subImageWrapper}>
            <img src={subImage2} alt={`${title} 서브2`} className={styles.image} />
          </div>
        </div>
      </div>
      
      <div className={styles.info}>
        {isEditing ? (
          <input
            className={styles.titleInput}
            value={tempTitle}
            onChange={(e) => setTempTitle(e.target.value)}
            onBlur={() => onRename?.(id, tempTitle)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') onRename?.(id, tempTitle);
            }}
            autoFocus
            onClick={(e) => e.stopPropagation()}
          />
        ) : (
          <h3 className={styles.title}>{title}</h3>
        )}
        <p className={styles.meta}>핀 {itemCount}개 · {lastUpdated}</p>
      </div>
    </div>
  );
}
