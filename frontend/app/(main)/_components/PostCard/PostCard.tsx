'use client';

import Link from 'next/link';
import { useState } from 'react';
import styles from './PostCard.module.css';

interface PostCardProps {
  id: number;
  imageUrl?: string;
  authorName: string;
  authorAvatar?: string;
  ratio?: 'square' | 'tall';
}

export default function PostCard({ id, imageUrl, authorName, authorAvatar, ratio = 'tall' }: PostCardProps) {
  const [isLiked, setIsLiked] = useState(false);
  // 사용자가 이미지를 추가하기 전까지 엑스박스(깨진 이미지)가 뜨지 않도록 임시 온라인 이미지를 넣습니다.
  const displayImage = imageUrl || '/dummy.jpg';

  const handleLike = (e: React.MouseEvent) => {
    e.preventDefault(); // Prevent navigating to the link
    setIsLiked(!isLiked);
  };
  
  return (
    <Link href={`/post/${id}`} className={`${styles.card} ${ratio === 'square' ? styles.ratioSquare : styles.ratioTall}`}>
      <div className={styles.imageWrapper}>
        <img src={displayImage} alt="게시물 이미지" className={styles.image} />
        
        <div className={styles.overlay} />

        <div className={styles.authorInfo}>
          {authorAvatar ? (
            <img src={authorAvatar} alt={authorName} className={styles.authorAvatar} />
          ) : (
            <div className={styles.authorAvatar} />
          )}
          <span className={styles.authorName}>{authorName}</span>
        </div>

        <button 
          className={`${styles.likeBtn} ${isLiked ? styles.liked : ''}`} 
          onClick={handleLike}
          aria-label="좋아요"
        >
          <svg viewBox="0 0 24 24" className={styles.heartIcon}>
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
          </svg>
        </button>
      </div>
    </Link>
  );
}
