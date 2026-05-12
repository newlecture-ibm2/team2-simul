'use client';

import React, { useState, useRef, useEffect } from 'react';
import Link from 'next/link';
import { getFeedPosts, FeedPost, toggleLike } from '../../../../lib/api/feedAPI';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '../../../../lib/stores/useAuthStore';
import { toast } from '@/lib/utils/toast';
import styles from './SwipeDeck.module.css';

// Dummy data using the specified images
const DUMMY_POSTS = [
  { id: 101, imageUrl: '/hero-1.jpg', authorName: '지수', authorAvatar: '/dummy.jpg' },
  { id: 102, imageUrl: '/hero-2.jpg', authorName: '태형', authorAvatar: '/recent.jpg' },
  { id: 103, imageUrl: '/hero-3.jpg', authorName: '민지', authorAvatar: '/temp.jpg' },
  { id: 104, imageUrl: '/hero-4.jpg', authorName: '정국', authorAvatar: '/dummy.jpg' },
  { id: 105, imageUrl: '/hero-5.jpg', authorName: '사나', authorAvatar: '/recent.jpg' },
];

interface SwipePost {
  id: string | number;
  imageUrl: string;
  authorName: string;
  authorAvatar: string;
}

export default function SwipeDeck() {
  const [posts, setPosts] = useState<SwipePost[]>(DUMMY_POSTS);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [exitDirection, setExitDirection] = useState<'left' | 'right' | 'up' | 'down' | null>(null);

  const cardRef = useRef<HTMLDivElement>(null);
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();

  useEffect(() => {
    async function loadPopularPosts() {
      try {
        const res = await getFeedPosts({ sort: 'popular', size: 10 });
        if (res && res.content && res.content.length > 0) {
          const mappedPosts = res.content.map(p => ({
            id: p.postId,
            imageUrl: p.imageUrl || '/dummy.jpg',
            authorName: p.nickname || '알 수 없음',
            authorAvatar: p.profileImageUrl || '/dummy.jpg'
          }));
          // 백엔드 데이터가 적을 수 있으므로, 기존의 예시 이미지(DUMMY_POSTS)를 뒤에 합쳐서 양을 늘립니다.
          setPosts([...mappedPosts, ...DUMMY_POSTS]);
        }
      } catch (err) {
        console.error('인기 게시물 불러오기 실패:', err);
      }
    }
    loadPopularPosts();
  }, []);

  // Constants for swipe logic
  const SWIPE_THRESHOLD = 80;

  const handlePointerDown = (e: React.PointerEvent) => {
    if (exitDirection) return; // Prevent interaction during animation
    setIsDragging(true);
    setDragStart({ x: e.clientX, y: e.clientY });
    e.currentTarget.setPointerCapture(e.pointerId);
  };

  const handlePointerMove = (e: React.PointerEvent) => {
    if (!isDragging) return;
    setDragOffset({
      x: e.clientX - dragStart.x,
      y: e.clientY - dragStart.y,
    });
  };

  const handlePointerUp = (e: React.PointerEvent) => {
    if (!isDragging) return;
    setIsDragging(false);

    const absX = Math.abs(dragOffset.x);
    const absY = Math.abs(dragOffset.y);

    // Determine swipe direction based on dominant axis
    if (absX > absY && absX > SWIPE_THRESHOLD && dragOffset.x > 0) {
      // Swiped right (Like)
      if (!isAuthenticated) {
        toast.error('좋아요를 누르려면 로그인이 필요합니다.');
        setDragOffset({ x: 0, y: 0 }); // Spring back
        return;
      }
      
      const currentPost = posts[((currentIndex % posts.length) + posts.length) % posts.length];
      if (typeof currentPost.id === 'string' && !currentPost.id.startsWith('dummy')) {
        toggleLike(currentPost.id).catch(err => console.error('좋아요 토글 실패:', err));
      }

      setExitDirection('right');
      triggerTransition(1);
    } else if (absY > absX && absY > SWIPE_THRESHOLD) {
      if (dragOffset.y < 0) {
        // Swiped up (Next)
        setExitDirection('up');
        triggerTransition(1);
      } else {
        // Swiped down (Prev)
        setExitDirection('down');
        triggerTransition(-1);
      }
    } else {
      // Spring back to center
      setDragOffset({ x: 0, y: 0 });
    }
  };

  const triggerTransition = (step: number) => {
    // Wait for the exit animation to finish before updating index
    setTimeout(() => {
      setCurrentIndex((prev) => prev + step);
      setExitDirection(null);
      setDragOffset({ x: 0, y: 0 });
    }, 300); // 300ms matches the CSS transition duration
  };

  const getCardStyle = (index: number) => {
    const isTopCard = index === currentIndex;
    
    if (isTopCard) {
      let x = dragOffset.x;
      let y = dragOffset.y;
      let rotation = x * 0.05; // Slight rotation based on X movement
      let opacity = 1;

      if (exitDirection) {
        // Apply exit animations
        if (exitDirection === 'right') {
          x = window.innerWidth;
          rotation = 15;
          opacity = 0;
        } else if (exitDirection === 'up') {
          y = -window.innerHeight;
          opacity = 0;
        } else if (exitDirection === 'down') {
          y = window.innerHeight;
          opacity = 0;
        }
      } else if (!isDragging) {
        x = 0;
        y = 0;
        rotation = 0;
      }

      return {
        transform: `translate3d(${x}px, ${y}px, 0) rotate(${rotation}deg)`,
        transition: isDragging ? 'none' : 'transform 0.3s ease, opacity 0.3s ease',
        zIndex: 10,
        opacity,
      };
    } else {
      // Stacked cards (up to 2 in each direction)
      const offsetIndex = index - currentIndex;
      const absOffset = Math.abs(offsetIndex);
      
      const yOffset = offsetIndex > 0 ? absOffset * 100 : -(absOffset * 100);
      const scale = 1 - (absOffset * 0.15); // 1->0.85, 2->0.7
      const zIndex = 10 - absOffset;
      const opacity = 1; // Fully opaque

      return {
        transform: `translate3d(0, ${yOffset}px, 0) scale(${scale})`,
        transition: 'transform 0.3s cubic-bezier(0.2, 0.8, 0.2, 1), opacity 0.3s ease',
        zIndex,
        opacity,
      };
    }
  };

  // Prevent default image drag behavior
  const handleDragStart = (e: React.DragEvent) => e.preventDefault();



  return (
    <div className={styles.deckContainer}>
      <div className={styles.header}>
        <h2 className={styles.title}>지금 내게 딱 맞는 룩은? 가볍게 넘겨보세요.</h2>
        <p className={styles.subtitle}>마음에 드는 룩은 오른쪽으로 스와이프 하세요! ❤️</p>
      </div>

      <div className={styles.deckWrapper}>
        {[-2, -1, 0, 1, 2].map((offset) => {
          if (posts.length === 0) return null;
          const virtualIndex = currentIndex + offset;
          const dataIndex = ((virtualIndex % posts.length) + posts.length) % posts.length;
          const post = posts[dataIndex];
          const isTopCard = offset === 0;

          return (
            <div
              key={virtualIndex}
              ref={isTopCard ? cardRef : null}
              className={`${styles.card} ${isTopCard && isDragging ? styles.dragging : ''}`}
              style={getCardStyle(virtualIndex)}
              onPointerDown={isTopCard ? handlePointerDown : undefined}
              onPointerMove={isTopCard ? handlePointerMove : undefined}
              onPointerUp={isTopCard ? handlePointerUp : undefined}
              onDragStart={handleDragStart}
              onClick={(e) => {
                 // 드래그 중이 아닐 때만 상세 페이지 이동 (단일 클릭)
                 if (isTopCard && !isDragging && Math.abs(dragOffset.x) < 5 && Math.abs(dragOffset.y) < 5) {
                    if (typeof post.id === 'string') {
                       router.push(`/post/${post.id}`);
                    }
                 }
              }}
            >
              <div className={styles.imageWrapper} style={{ cursor: 'pointer' }}>
                <img src={post.imageUrl} alt="게시물 이미지" className={styles.image} />

                <div className={styles.authorInfo}>
                  <img src={post.authorAvatar} alt="프로필" className={styles.authorAvatar} />
                  <span className={styles.authorName}>{post.authorName}</span>
                </div>


              </div>
            </div>
          );
        })}

        {/* Like Indicator Overlay - Centered on Deck */}
        {dragOffset.x > SWIPE_THRESHOLD && (
           <div className={styles.likeIndicator}>
             <div className={styles.heartIcon}>❤️</div>
           </div>
        )}
      </div>
    </div>
  );
}
