'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import PostCard from '../PostCard';
import styles from './FeedGrid.module.css';

const AUTHORS = ['지수', '민준', '서연', '하은', '도윤', '예린', '시우', '수빈', '지호', '유나', '태현', '소율'];

interface Post {
  id: number;
  authorName: string;
  ratio: 'square' | 'tall';
}

export default function FeedGrid() {
  const [posts, setPosts] = useState<Post[]>(
    Array.from({ length: 8 }, (_, i) => ({
      id: i + 1,
      authorName: AUTHORS[i % AUTHORS.length],
      // Masonry pattern logic: random mix
      ratio: (i % 3 === 0 || i % 7 === 0) ? 'square' : 'tall'
    }))
  );
  const [isLoading, setIsLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const loadMoreRef = useRef<HTMLDivElement | null>(null);

  const loadMorePosts = useCallback(() => {
    if (isLoading || !hasMore) return;
    setIsLoading(true);

    // Simulate network request
    setTimeout(() => {
      setPosts(prev => {
        const nextId = prev.length + 1;
        if (nextId > 60) {
          setHasMore(false); // Stop after 60 items for demo
          return prev;
        }
        
        const newPosts: Post[] = Array.from({ length: 8 }, (_, i) => {
          const id = nextId + i;
          return {
            id,
            authorName: AUTHORS[id % AUTHORS.length],
            ratio: (id % 3 === 0 || id % 7 === 0) ? 'square' : 'tall'
          };
        });
        return [...prev, ...newPosts];
      });
      setIsLoading(false);
    }, 800); // 800ms delay to simulate loading
  }, [isLoading, hasMore]);

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

  const leftColumn = posts.filter((_, i) => i % 2 === 0);
  const rightColumn = posts.filter((_, i) => i % 2 !== 0);

  return (
    <>
      <div className={styles.feedGrid}>
        <div className={styles.column}>
          {leftColumn.map((post) => (
            <PostCard
              key={post.id}
              id={post.id}
              authorName={post.authorName}
              ratio={post.ratio}
            />
          ))}
        </div>
        <div className={styles.column}>
          {rightColumn.map((post) => (
            <PostCard
              key={post.id}
              id={post.id}
              authorName={post.authorName}
              ratio={post.ratio}
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
