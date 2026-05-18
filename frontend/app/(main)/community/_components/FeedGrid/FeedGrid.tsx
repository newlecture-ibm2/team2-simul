'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import PostCard from '../PostCard';
import styles from './FeedGrid.module.css';
import { getFeedPosts, FeedPost } from '@/lib/api/feedAPI';

/* ── 더미 데이터 (API 데이터가 없을 때 Fallback) ── */
const AUTHORS = ['지수', '민준', '서연', '하은', '도윤', '예린', '시우', '수빈', '지호', '유나', '태현', '소율'];

function generateDummyPosts(startId: number, count: number): FeedPost[] {
  return Array.from({ length: count }, (_, i) => {
    const id = startId + i;
    return {
      postId: `dummy-${id}`,
      userId: `user-${id}`,
      nickname: AUTHORS[id % AUTHORS.length],
      profileImageUrl: null,
      imageUrl: '/dummy.jpg',
      tags: ['fashion', 'ootd', 'simul'].slice(0, (id % 3) + 1),
      caption: '',
      likeCount: Math.floor(Math.random() * 100),
      isLiked: false,
      createdAt: new Date().toISOString(),
    };
  });
}

/* ── 컴포넌트 ── */
interface FeedGridProps {
  tab?: string;
  sort?: string;
}

export default function FeedGrid({ tab = 'all', sort = 'recent' }: FeedGridProps) {
  const [posts, setPosts] = useState<FeedPost[]>([]);
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [useDummy, setUseDummy] = useState(false);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const loadMoreRef = useRef<HTMLDivElement | null>(null);

  // 탭이나 정렬 변경 시 초기화
  useEffect(() => {
    setPosts([]);
    setPage(0);
    setHasMore(true);
    setUseDummy(false);
  }, [tab, sort]);

  const loadMorePosts = useCallback(async () => {
    if (isLoading || !hasMore) return;
    setIsLoading(true);

    // 더미 모드일 때
    if (useDummy) {
      setTimeout(() => {
        setPosts(prev => {
          if (prev.length >= 60) {
            setHasMore(false);
            return prev;
          }
          return [...prev, ...generateDummyPosts(prev.length + 1, 8)];
        });
        setPage(prev => prev + 1);
        setIsLoading(false);
      }, 600);
      return;
    }

    // 실제 API 호출
    try {
      const data = await getFeedPosts({ tab, sort, page, size: 20 });
      
      if (data.content.length === 0 && page === 0) {
        // 실제 데이터가 없으면 Empty State를 렌더링하기 위해 posts를 비움
        setPosts([]);
        setHasMore(false);
      } else {
        setPosts(prev => page === 0 ? data.content : [...prev, ...data.content]);
        setHasMore(!data.last);
        setPage(prev => prev + 1);
      }
    } catch {
      // API 실패 → 더미 모드 전환
      if (page === 0) {
        setUseDummy(true);
        setPosts(generateDummyPosts(1, 8));
        setPage(1);
      } else {
        setHasMore(false);
      }
    } finally {
      setIsLoading(false);
    }
  }, [isLoading, hasMore, useDummy, tab, sort, page]);

  // Intersection Observer 설정
  useEffect(() => {
    observerRef.current = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && hasMore) {
        loadMorePosts();
      }
    }, { threshold: 0.1 });

    if (loadMoreRef.current) {
      observerRef.current.observe(loadMoreRef.current);
    }

    return () => {
      if (observerRef.current) observerRef.current.disconnect();
    };
  }, [loadMorePosts, hasMore]);

  // 2열 메이슨리 레이아웃 분배
  const leftColumn = posts.filter((_, i) => i % 2 === 0);
  const rightColumn = posts.filter((_, i) => i % 2 !== 0);

  const getRatio = (index: number): 'square' | 'tall' =>
    (index % 3 === 0 || index % 7 === 0) ? 'square' : 'tall';

  return (
    <>
      {posts.length === 0 && !isLoading && (
        <div className={styles.emptyState}>
          <p className={styles.emptyIcon}>📸</p>
          <p className={styles.emptyText}>아직 게시물이 없어요</p>
          <p className={styles.emptySubtext}>첫 번째 시착 결과를 공유해 보세요!</p>
        </div>
      )}

      <div className={styles.feedGrid}>
        <div className={styles.column}>
          {leftColumn.map((post, i) => (
            <PostCard
              key={post.postId}
              postId={post.postId}
              imageUrl={post.imageUrl}
              authorName={post.nickname}
              authorAvatar={post.profileImageUrl}
              authorId={post.userId}
              tags={post.tags}
              likeCount={post.likeCount}
              isLiked={post.isLiked}
              ratio={getRatio(i * 2)}
            />
          ))}
        </div>
        <div className={styles.column}>
          {rightColumn.map((post, i) => (
            <PostCard
              key={post.postId}
              postId={post.postId}
              imageUrl={post.imageUrl}
              authorName={post.nickname}
              authorAvatar={post.profileImageUrl}
              authorId={post.userId}
              tags={post.tags}
              likeCount={post.likeCount}
              isLiked={post.isLiked}
              ratio={getRatio(i * 2 + 1)}
            />
          ))}
        </div>
      </div>
      {hasMore && (
        <div ref={loadMoreRef} className={styles.loader}>
          {isLoading && <div className={styles.spinner} />}
        </div>
      )}
    </>
  );
}
