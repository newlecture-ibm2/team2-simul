'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { toggleLike, getPostDetail } from '../../../../lib/api/feedAPI';
import { useAuthStore } from '../../../../lib/stores/useAuthStore';
import styles from './page.module.css';

export interface PostDetailData {
  postId: string;
  userId: string;
  nickname: string;
  profileImageUrl: string | null;
  images: string[];
  tags: string[];
  caption: string;
  likeCount: number;
  viewCount: number;
  isLiked: boolean;
  createdAt: string;
}

export default function PostDetailPage() {
  const [post, setPost] = useState<PostDetailData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  
  const params = useParams();
  const router = useRouter();
  const postId = params.id as string;
  const { isAuthenticated } = useAuthStore();
  const [currentImgIndex, setCurrentImgIndex] = useState(0);

  useEffect(() => {
    async function loadPost() {
      try {
        const data = await getPostDetail(postId) as PostDetailData;
        setPost(data);
        setIsLiked(data.isLiked);
        setLikeCount(data.likeCount);
      } catch (err: unknown) {
        const errorMsg = err instanceof Error ? err.message : String(err);
        if (errorMsg.includes('403') || errorMsg.includes('404') || errorMsg.includes('ERR-002') || errorMsg.includes('ERR-003')) {
           setError('접근할 수 없거나 삭제된 게시물입니다.');
        } else {
           setError('게시물을 불러오는데 실패했습니다.');
        }
      } finally {
        setIsLoading(false);
      }
    }
    
    if (postId && postId !== 'dummy') {
      loadPost();
    } else {
      setIsLoading(false);
    }
  }, [postId]);

  const handleLike = async () => {
    if (!isAuthenticated) {
      alert('좋아요를 누르려면 로그인이 필요합니다.');
      return;
    }

    const previousIsLiked = isLiked;
    const previousLikeCount = likeCount;

    setIsLiked(!previousIsLiked);
    setLikeCount(prev => previousIsLiked ? prev - 1 : prev + 1);

    try {
      if (postId !== 'dummy' && !postId.startsWith('dummy')) {
         await toggleLike(postId);
      }
    } catch (err) {
      setIsLiked(previousIsLiked);
      setLikeCount(previousLikeCount);
      console.error('좋아요 토글 실패:', err);
    }
  };

  if (isLoading) return <div className={styles.container}><div style={{padding: '20px', textAlign: 'center'}}>로딩 중...</div></div>;
  if (error) return <div className={styles.container}><div style={{padding: '20px', textAlign: 'center', color: 'red'}}>{error}</div></div>;
  if (!post) return null;

  // format date roughly
  const dateObj = new Date(post.createdAt);
  const dateStr = `${dateObj.getFullYear()}.${String(dateObj.getMonth() + 1).padStart(2, '0')}.${String(dateObj.getDate()).padStart(2, '0')}`;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button onClick={() => router.back()} className={styles.iconBtn} aria-label="뒤로가기">
          <img src="/icons/arrow-left.png" alt="Back" className={styles.icon} />
        </button>
        <button className={styles.iconBtn} aria-label="공유하기">
          <img src="/icons/square.and.arrow.up.png" alt="Share" className={styles.icon} />
        </button>
      </div>

      <div className={styles.postDetail}>
        <div className={styles.imageCarouselContainer}>
          <div 
            className={styles.imageCarousel}
            onScroll={(e) => {
              const scrollLeft = e.currentTarget.scrollLeft;
              const width = e.currentTarget.clientWidth;
              if (width > 0) {
                setCurrentImgIndex(Math.round(scrollLeft / width));
              }
            }}
          >
            {post.images && post.images.length > 0 ? (
              post.images.map((url: string, idx: number) => (
                <div key={idx} className={styles.imageSlide}>
                  <img src={url} alt={`게시물 이미지 ${idx + 1}`} className={styles.image} />
                </div>
              ))
            ) : (
              <div className={styles.imageSlide}>
                <img src="/dummy.jpg" alt="기본 이미지" className={styles.image} />
              </div>
            )}
          </div>
          {post.images && post.images.length > 1 && (
            <div className={styles.carouselIndicators}>
              {post.images.map((_: string, idx: number) => (
                <div 
                  key={idx} 
                  className={`${styles.indicator} ${currentImgIndex === idx ? styles.indicatorActive : ''}`} 
                />
              ))}
            </div>
          )}
        </div>

        <div className={styles.contentSection}>
          <div className={styles.authorRow}>
            {post.profileImageUrl ? (
               <img src={post.profileImageUrl} alt="아바타" className={styles.authorAvatarImg} style={{width: '36px', height: '36px', borderRadius: '50%', objectFit: 'cover'}} />
            ) : (
               <span className={styles.authorAvatar}>🧑</span>
            )}
            <div className={styles.authorInfo}>
              <div className={styles.authorName}>{post.nickname}</div>
              <div className={styles.authorMeta}>{dateStr}</div>
            </div>
            <button className={styles.followBtn}>팔로우</button>
          </div>

          <p className={styles.caption}>
            {post.caption}
          </p>
          
          {post.tags && post.tags.length > 0 && (
             <div style={{display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '16px'}}>
                {post.tags.map((tag: string) => (
                   <span key={tag} style={{color: 'var(--color-primary)', fontSize: '14px', fontWeight: '500'}}>#{tag}</span>
                ))}
             </div>
          )}

          <div className={styles.stats}>
            <button className={styles.statItem} onClick={handleLike}>
              <img 
                src={isLiked ? "/icons/heart-filled.png" : "/icons/heart.png"} 
                alt="Like" 
                className={styles.statIcon} 
                onError={(e) => {
                   (e.target as HTMLImageElement).src = "/icons/heart.png";
                }}
              />
              <span>{likeCount}</span>
            </button>
            <div className={styles.statItem}>
              <img src="/icons/bubble.png" alt="Comment" className={styles.statIcon} />
              <span>0</span>
            </div>
            <div className={styles.statItem} style={{marginLeft: 'auto', color: '#999', fontSize: '13px'}}>
               조회 {post.viewCount}
            </div>
          </div>
          
          <button className={styles.reportBtn}>🚨 게시물 신고하기</button>
        </div>
      </div>
    </div>
  );
}
