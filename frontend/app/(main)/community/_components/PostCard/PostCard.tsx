'use client';

import Link from 'next/link';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { toggleLike } from '@/lib/api/feedAPI';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import styles from './PostCard.module.css';

interface PostCardProps {
  postId: string;
  imageUrl?: string | null;
  authorName: string;
  authorAvatar?: string | null;
  tags?: string[];
  likeCount?: number;
  isLiked?: boolean;
  ratio?: 'square' | 'tall';
}

export default function PostCard({ 
  postId, 
  imageUrl, 
  authorName, 
  authorAvatar, 
  tags = [],
  likeCount = 0,
  isLiked: initialLiked = false,
  ratio = 'tall' 
}: PostCardProps) {
  const router = useRouter();
  const [isLiked, setIsLiked] = useState(initialLiked);
  const [currentLikeCount, setCurrentLikeCount] = useState(likeCount);
  const { isAuthenticated } = useAuthStore();
  const displayImage = imageUrl || '/dummy.jpg';

  const handleCardClick = () => {
    router.push(`/post/${postId}`);
  };

  const handleLike = async (e: React.MouseEvent) => {
    e.stopPropagation();

    if (!isAuthenticated) {
      alert('좋아요를 누르려면 로그인이 필요합니다.');
      return;
    }

    const previousIsLiked = isLiked;
    const previousLikeCount = currentLikeCount;

    // 낙관적 업데이트
    setIsLiked(!previousIsLiked);
    setCurrentLikeCount(prev => previousIsLiked ? prev - 1 : prev + 1);
    
    try {
      await toggleLike(postId);
    } catch (error) {
      // 실패 시 롤백
      setIsLiked(previousIsLiked);
      setCurrentLikeCount(previousLikeCount);
      console.error('좋아요 토글 실패:', error);
    }
  };
  
  return (
    <div
      className={`${styles.card} ${ratio === 'square' ? styles.ratioSquare : styles.ratioTall}`}
      onClick={handleCardClick}
      style={{ cursor: 'pointer' }}
    >
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

        {tags.length > 0 && (
          <div className={styles.tagList}>
            {tags.slice(0, 3).map((tag) => (
              <Link
                key={tag}
                href={`/search?q=%23${tag}&type=tag`}
                style={{ textDecoration: 'none' }}
                onClick={(e) => e.stopPropagation()}
              >
                <span className={styles.tagChip}>#{tag}</span>
              </Link>
            ))}
          </div>
        )}

        <button 
          className={`${styles.likeBtn} ${isLiked ? styles.liked : ''}`} 
          onClick={handleLike}
          aria-label="좋아요"
        >
          <svg viewBox="0 0 24 24" className={styles.heartIcon}>
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
          </svg>
          {currentLikeCount > 0 && (
            <span className={styles.likeCount}>{currentLikeCount}</span>
          )}
        </button>
      </div>
    </div>
  );
}
