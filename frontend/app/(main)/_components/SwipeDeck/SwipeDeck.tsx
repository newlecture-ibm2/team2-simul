'use client';

import React, { useState, useRef, useEffect, useCallback } from 'react';
import { getFeedPosts, FeedPost, toggleLike } from '../../../../lib/api/feedAPI';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '../../../../lib/stores/useAuthStore';
import { toast } from '@/lib/utils/toast';
import styles from './SwipeDeck.module.css';

/* ── 폴백 전용 더미 데이터 (실제 게시물이 0개일 때만 렌더링) ── */
const FALLBACK_POSTS: SwipePost[] = [
  { id: 101, imageUrl: '/hero-1.jpg', authorName: '지수', authorAvatar: '/dummy.jpg', isLiked: false, authorId: 'dummy-1' },
  { id: 102, imageUrl: '/hero-2.jpg', authorName: '태형', authorAvatar: '/recent.jpg', isLiked: false, authorId: 'dummy-2' },
  { id: 103, imageUrl: '/hero-3.jpg', authorName: '민지', authorAvatar: '/temp.jpg', isLiked: false, authorId: 'dummy-3' },
  { id: 104, imageUrl: '/hero-4.jpg', authorName: '정국', authorAvatar: '/dummy.jpg', isLiked: false, authorId: 'dummy-4' },
  { id: 105, imageUrl: '/hero-5.jpg', authorName: '사나', authorAvatar: '/recent.jpg', isLiked: false, authorId: 'dummy-5' },
];

interface SwipePost {
  id: string | number;
  imageUrl: string;
  authorName: string;
  authorAvatar: string;
  isLiked?: boolean;
  authorId?: string;
}

/** Fisher-Yates 셔플 알고리즘 */
function shuffleArray<T>(array: T[]): T[] {
  const shuffled = [...array];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
}

export default function SwipeDeck() {
  const [posts, setPosts] = useState<SwipePost[]>(FALLBACK_POSTS);
  const [isFallbackMode, setIsFallbackMode] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [exitDirection, setExitDirection] = useState<'left' | 'right' | 'up' | 'down' | null>(null);
  const [showDoubleTapHeart, setShowDoubleTapHeart] = useState(false);

  const cardRef = useRef<HTMLDivElement>(null);
  const lastTapRef = useRef<number>(0);
  const router = useRouter();
  const { isAuthenticated, user } = useAuthStore();

  // 최초 진입 시 게시물 50개를 불러온 뒤 셔플 (무한 랜덤 스와이프)
  useEffect(() => {
    async function loadAndShufflePosts() {
      try {
        const res = await getFeedPosts({ sort: 'recent', size: 50 });
        if (res && res.content && res.content.length > 0) {
          const mappedPosts: SwipePost[] = res.content.map(p => ({
            id: p.postId,
            imageUrl: p.imageUrl || '/dummy.jpg',
            authorName: p.nickname || '알 수 없음',
            authorAvatar: p.profileImageUrl || '/dummy.jpg',
            isLiked: p.isLiked,
            authorId: p.userId,
          }));
          // 실제 데이터가 있으면 셔플 후 세팅 (폴백 모드 해제)
          setPosts(shuffleArray(mappedPosts));
          setIsFallbackMode(false);
        }
        // 데이터가 0개이면 기존 FALLBACK_POSTS가 유지됨
      } catch (err) {
        console.error('게시물 불러오기 실패:', err);
        // API 에러 시에도 폴백 데이터가 유지됨
      }
    }
    loadAndShufflePosts();
  }, []);

  // Constants for swipe logic
  const SWIPE_THRESHOLD = 80;
  const DOUBLE_TAP_DELAY = 300; // ms

  /** 현재 보고 있는 카드의 게시물을 가져오는 헬퍼 */
  const getCurrentPost = useCallback(() => {
    if (posts.length === 0) return null;
    const dataIndex = ((currentIndex % posts.length) + posts.length) % posts.length;
    return posts[dataIndex];
  }, [posts, currentIndex]);

  /** 좋아요 처리 공통 함수 (우측 스와이프 & 더블 탭 공용) */
  const handleLike = useCallback(() => {
    if (!isAuthenticated) {
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('simul_auth_modal_event', { detail: { isOpen: true } }));
      }
      return false;
    }

    const currentPost = getCurrentPost();
    if (!currentPost) return false;

    // 실제 게시물(UUID 문자열)에 대해서만 API 호출
    if (typeof currentPost.id === 'string' && !currentPost.isLiked) {
      toggleLike(currentPost.id).catch(err => console.error('좋아요 토글 실패:', err));
      
      // 로컬 상태 즉시 업데이트 (연속 탭/스와이프 시 중복 방지)
      setPosts(prev => {
        const newPosts = [...prev];
        const index = newPosts.findIndex(p => p.id === currentPost.id);
        if (index !== -1) {
          newPosts[index] = { ...newPosts[index], isLiked: true };
        }
        return newPosts;
      });
    }
    return true;
  }, [isAuthenticated, getCurrentPost]);

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

  const handlePointerUp = () => {
    if (!isDragging) return;
    setIsDragging(false);

    const absX = Math.abs(dragOffset.x);
    const absY = Math.abs(dragOffset.y);

    // Determine swipe direction based on dominant axis
    if (absX > absY && absX > SWIPE_THRESHOLD && dragOffset.x > 0) {
      // Swiped right (Like) ❤️
      if (!handleLike()) {
        setDragOffset({ x: 0, y: 0 }); // Spring back on auth failure
        return;
      }
      setExitDirection('right');
      triggerTransition(1);
    } else if (absX > absY && absX > SWIPE_THRESHOLD && dragOffset.x < 0) {
      // Swiped left (Pass) 🥲
      setExitDirection('left');
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

  const clickTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  /** 더블 탭 & 단일 탭 분기 핸들러 */
  const handleCardTap = () => {
    const now = Date.now();
    if (now - lastTapRef.current < DOUBLE_TAP_DELAY) {
      // 더블 탭 감지!
      if (clickTimeoutRef.current) clearTimeout(clickTimeoutRef.current);
      if (handleLike()) {
        setShowDoubleTapHeart(true);
        setTimeout(() => setShowDoubleTapHeart(false), 600);
      }
      lastTapRef.current = 0; // 리셋
    } else {
      lastTapRef.current = now;
      clickTimeoutRef.current = setTimeout(() => {
        if (lastTapRef.current !== 0) {
          // 단일 클릭 처리 → 상세 페이지 이동
          const currentPost = getCurrentPost();
          if (currentPost && typeof currentPost.id === 'string') {
            router.push(`/post/${currentPost.id}`);
          }
          lastTapRef.current = 0;
        }
      }, DOUBLE_TAP_DELAY);
    }
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
        } else if (exitDirection === 'left') {
          x = -window.innerWidth;
          rotation = -15;
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
              onClick={isTopCard ? () => {
                if (!isDragging && Math.abs(dragOffset.x) < 5 && Math.abs(dragOffset.y) < 5) {
                  handleCardTap();
                }
              } : undefined}
            >
              <div className={styles.imageWrapper} style={{ cursor: 'pointer' }}>
                <img src={post.imageUrl} alt="게시물 이미지" className={styles.image} />

                <div 
                  className={styles.authorInfo}
                  onClick={(e) => {
                    e.stopPropagation();
                    if (!post.authorId) return;
                    
                    if (isAuthenticated && user && (user.userId === post.authorId || user.id === post.authorId)) {
                      // 로그인 사용자가 본인의 게시물을 클릭한 경우
                      router.push('/profile');
                    } else {
                      // 비로그인 사용자 또는 타인의 게시물을 클릭한 경우
                      router.push(`/profile/${post.authorId}`);
                    }
                  }}
                  style={{ cursor: 'pointer' }}
                >
                  <img src={post.authorAvatar} alt="프로필" className={styles.authorAvatar} />
                  <span className={styles.authorName}>{post.authorName}</span>
                </div>

                {/* 폴백 모드일 때 샘플 배지 표시 */}
                {isFallbackMode && (
                  <div className={styles.sampleBadge}>샘플</div>
                )}
              </div>
            </div>
          );
        })}

        {/* Like Indicator Overlay (우측 드래그 시) ❤️ */}
        {dragOffset.x > SWIPE_THRESHOLD && (
           <div className={styles.likeIndicator}>
             <div className={styles.heartIcon}>❤️</div>
           </div>
        )}

        {/* Pass Indicator Overlay (좌측 드래그 시) */}
        {dragOffset.x < -SWIPE_THRESHOLD && (
           <div className={styles.passIndicator}>
             <div className={styles.passIcon}>
                <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.9)" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" style={{ filter: 'drop-shadow(0px 4px 8px rgba(0,0,0,0.4))' }}>
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
             </div>
           </div>
        )}

        {/* Double Tap Like Indicator ❤️ (더블 탭 시 동일한 하트 재사용) */}
        {showDoubleTapHeart && (
           <div className={styles.doubleTapHeart}>
             <div className={styles.heartIcon}>❤️</div>
           </div>
        )}
      </div>
    </div>
  );
}
