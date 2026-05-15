'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { getCurrentUser } from '@/lib/api/authAPI';
import { getUserPosts, getLikedPosts, FeedPost } from '@/lib/api/feedAPI';
import { useAuthStore, User } from '@/lib/stores/useAuthStore';
import styles from './page.module.css';

export default function ProfilePage() {
  const [activeTab, setActiveTab] = useState<'게시물' | '좋아요'>('게시물');
  const router = useRouter();
  const { setUser } = useAuthStore();

  // 1. 내 정보 조회
  const { data: user, isLoading: isUserLoading } = useQuery<User>({
    queryKey: ['me'],
    queryFn: getCurrentUser,
  });

  // 2. 내 게시물 목록 조회
  const { data: postsData, isLoading: isPostsLoading } = useQuery({
    queryKey: ['userPosts', user?.userId],
    queryFn: () => user ? getUserPosts(user.userId) : Promise.reject('User not logged in'),
    enabled: !!user?.userId,
  });

  // 3. 내가 좋아요한 게시물 목록 조회
  const { data: likedPostsData } = useQuery({
    queryKey: ['likedPosts'],
    queryFn: () => getLikedPosts(),
    enabled: !!user?.userId && activeTab === '좋아요',
  });

  // 가져온 정보를 전역 스토어에 동기화
  useEffect(() => {
    if (user) {
      setUser(user);
    }
  }, [user, setUser]);

  // 3. 비로그인 상태면 로그인 페이지로 리다이렉트
  useEffect(() => {
    if (!isUserLoading && !user) {
      router.push('/login');
    }
  }, [user, isUserLoading, router]);

  if (isUserLoading) return <div className={styles.loading}>로딩 중...</div>;
  if (!user) return null; // 리다이렉트 중에는 아무것도 렌더링하지 않음

  return (
    <div className={styles.profilePage}>
      <div className={styles.profileFrame}>
        {/* Immersive Hero Section */}
        <div className={styles.heroSection}>
          <img src={user?.bannerImageUrl || "/images/login-bg.png"} alt="Profile Banner" className={styles.heroBg} />
          <div className={styles.heroOverlay}></div>

          {/* Hero Content (Bottom aligned) */}
          <div className={styles.heroContent}>
            <h1 className={styles.heroName}>{user?.nickname || '사용자'}</h1>
            <p className={styles.heroUsername}>{user?.bio || '반갑습니다!'}</p>

            <div className={styles.heroActions}>
              <Link href="/profile/edit" className={styles.flexLink}>
                <button className={styles.heroBtn}>프로필 편집</button>
              </Link>
              <Link href="/settings" className={styles.circleLink}>
                <button className={styles.circleBtn}>⚙️</button>
              </Link>
            </div>

            <div className={styles.heroStats}>
              <div 
                className={styles.statItem} 
                style={{ cursor: 'pointer' }}
                onClick={() => router.push('/profile/followings')}
              >
                <span className={styles.statNum}>{user?.followingCount || 0}</span>
                <span className={styles.statText}>팔로잉</span>
              </div>
              <div 
                className={styles.statItem} 
                style={{ cursor: 'pointer' }}
                onClick={() => router.push('/profile/followers')}
              >
                <span className={styles.statNum}>{user?.followerCount || 0}</span>
                <span className={styles.statText}>팔로워</span>
              </div>
              <div className={styles.statItem}>
                <span className={styles.statNum}>{user?.postCount || 0}</span>
                <span className={styles.statText}>게시물</span>
              </div>
            </div>
          </div>
        </div>

        <div className={styles.bottomSection}>
          {/* Tabs */}
          <div className={styles.tabBar}>
            <button 
              className={`${styles.tabBtn} ${activeTab === '게시물' ? styles.activeTab : ''}`}
              onClick={() => setActiveTab('게시물')}
            >
              게시물
            </button>
            <button 
              className={`${styles.tabBtn} ${activeTab === '좋아요' ? styles.activeTab : ''}`}
              onClick={() => setActiveTab('좋아요')}
            >
              좋아요
            </button>
          </div>

          {/* Grid Content */}
          <div className={styles.gridContent}>
            {activeTab === '게시물' && (
              <div className={styles.postGrid}>
                {postsData?.content.length === 0 && (
                  <div className={styles.emptyMsg}>게시물이 없습니다.</div>
                )}
                {postsData?.content.map((post) => (
                  <Link key={post.postId} href={`/post/${post.postId}`} className={styles.gridItem}>
                    <img 
                      src={post.imageUrl || "/recent.jpg"} 
                      alt="Post" 
                      className={styles.gridImage} 
                    />
                  </Link>
                ))}
              </div>
            )}
            
            {activeTab === '좋아요' && (
              <div className={styles.postGrid}>
                {likedPostsData?.content.length === 0 && (
                  <div className={styles.emptyMsg}>좋아요한 게시물이 없습니다.</div>
                )}
                {likedPostsData?.content.map((post: FeedPost) => (
                  <Link key={post.postId} href={`/post/${post.postId}`} className={styles.gridItem}>
                    <img 
                      src={post.imageUrl || "/recent.jpg"} 
                      alt="Liked Post" 
                      className={styles.gridImage} 
                    />
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
