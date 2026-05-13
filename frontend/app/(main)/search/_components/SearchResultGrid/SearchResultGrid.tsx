'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import PostCard from '../PostCard';
import styles from './SearchResultGrid.module.css';
import { searchPosts, SearchType, FeedPostResponse } from '../../../../../lib/api/searchAPI';

interface SearchResultGridProps {
  query: string;
  type: SearchType;
}

export default function SearchResultGrid({ query, type }: SearchResultGridProps) {
  const [posts, setPosts] = useState<FeedPostResponse[]>([]);
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const loadMoreRef = useRef<HTMLDivElement | null>(null);

  // 쿼리나 타입이 변경되면 초기화
  useEffect(() => {
    setPosts([]);
    setPage(0);
    setHasMore(true);
  }, [query, type]);

  const loadMorePosts = useCallback(async () => {
    if (isLoading || !hasMore || !query.trim()) return;
    setIsLoading(true);

    try {
      const data = await searchPosts(query.trim(), type, page, 20);
      
      if (data.content.length === 0 && page === 0) {
        setPosts([]);
        setHasMore(false);
      } else {
        setPosts(prev => page === 0 ? data.content : [...prev, ...data.content]);
        setHasMore(!data.last);
        setPage(prev => prev + 1);
      }
    } catch {
      if (page !== 0) {
        setHasMore(false);
      }
    } finally {
      setIsLoading(false);
    }
  }, [isLoading, hasMore, query, type, page]);

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

  if (!query.trim()) {
    return null;
  }

  // 2열 메이슨리 레이아웃 분배
  const leftColumn = posts.filter((_, i) => i % 2 === 0);
  const rightColumn = posts.filter((_, i) => i % 2 !== 0);

  const getRatio = (index: number): 'square' | 'tall' =>
    (index % 3 === 0 || index % 7 === 0) ? 'square' : 'tall';

  return (
    <>
      {posts.length === 0 && !isLoading && (
        <div className={styles.emptyState}>
          <p className={styles.emptyIcon}>🔍</p>
          <p className={styles.emptyText}>검색 결과가 없어요</p>
          <p className={styles.emptySubtext}>다른 검색어로 다시 시도해 보세요.</p>
        </div>
      )}

      {posts.length > 0 && (
        <div className={styles.feedGrid}>
          <div className={styles.column}>
            {leftColumn.map((post, i) => (
              <PostCard
                key={post.postId}
                postId={post.postId}
                imageUrl={post.imageUrl}
                authorName={post.nickname}
                authorAvatar={post.profileImageUrl}
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
                tags={post.tags}
                likeCount={post.likeCount}
                isLiked={post.isLiked}
                ratio={getRatio(i * 2 + 1)}
              />
            ))}
          </div>
        </div>
      )}
      
      {hasMore && (
        <div ref={loadMoreRef} className={styles.loader}>
          {isLoading && <div className={styles.spinner} />}
        </div>
      )}
    </>
  );
}
