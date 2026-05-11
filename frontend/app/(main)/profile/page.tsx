'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { getCurrentUser } from '@/lib/api/authAPI';
import { User } from '@/lib/stores/useAuthStore';
import styles from './page.module.css';

export default function ProfilePage() {
  const [activeTab, setActiveTab] = useState<'게시물' | '옷장'>('게시물');
  const router = useRouter();

  const { data: user, isLoading } = useQuery<User>({
    queryKey: ['me'],
    queryFn: getCurrentUser,
  });

  if (isLoading) return <div className={styles.loading}>로딩 중...</div>;

  return (
    <div className={styles.profilePage}>
      <div className={styles.profileFrame}>
        {/* Immersive Hero Section */}
        <div className={styles.heroSection}>
          <img src={user?.profileImageUrl || "/profile.jpg"} alt="Profile Background" className={styles.heroBg} />
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
                <span className={styles.statNum}>0</span>
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
              className={`${styles.tabBtn} ${activeTab === '옷장' ? styles.activeTab : ''}`}
              onClick={() => setActiveTab('옷장')}
            >
              옷장
            </button>
          </div>

          {/* Grid Content */}
          <div className={styles.gridContent}>
            {activeTab === '게시물' && (
              <div className={styles.postGrid}>
                {Array.from({ length: 24 }, (_, i) => (
                  <Link key={i} href={`/post/${i + 1}`} className={styles.gridItem}>
                    <img src="/recent.jpg" alt="Post" className={styles.gridImage} />
                  </Link>
                ))}
              </div>
            )}
            
            {activeTab === '옷장' && (
              <div className={styles.postGrid}>
                {Array.from({ length: 24 }, (_, i) => (
                  <Link key={i} href="/closet" className={styles.gridItem}>
                    <img src="/clothes.png" alt="Closet item" className={styles.gridImage} />
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
