'use client';

import { useState, useEffect, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toggleLike, getPostDetail, deletePost, getComments } from '../../../../lib/api/feedAPI';
import { checkIsFollowing, followUser, unfollowUser } from '@/lib/api/userAPI';
import { useAuthStore } from '../../../../lib/stores/useAuthStore';
import { toast } from '@/lib/utils/toast';
import ConfirmModal from './_components/ConfirmModal/ConfirmModal';
import styles from './page.module.css';
import CommentSection from './_components/CommentSection/CommentSection';
import ReportModal from './_components/ReportModal/ReportModal';
import DeleteConfirmModal from './_components/DeleteConfirmModal/DeleteConfirmModal';
import LikeListModal from './_components/LikeListModal/LikeListModal';
import { reportPost } from '../../../../lib/api/feedAPI';


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
  commentCount: number;
  isLiked: boolean;
  reportCount: number;
  isWarned: boolean;
  createdAt: string;
}

export default function PostDetailPage() {
  const [post, setPost] = useState<PostDetailData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [showMenu, setShowMenu] = useState(false);
  
  const params = useParams();
  const router = useRouter();
  const postId = params.id as string;
  const { isAuthenticated, user } = useAuthStore();
  const [currentImgIndex, setCurrentImgIndex] = useState(0);

  const [isReportModalOpen, setIsReportModalOpen] = useState(false);
  const [isReporting, setIsReporting] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isLikeListModalOpen, setIsLikeListModalOpen] = useState(false);
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);

  const scrollRef = useRef<HTMLDivElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [startX, setStartX] = useState(0);
  const [scrollLeft, setScrollLeft] = useState(0);
  
  const [confirmModal, setConfirmModal] = useState({ 
    isOpen: false, 
    title: '', 
    description: '', 
    confirmText: '', 
    onConfirm: () => {} 
  });

  const queryClient = useQueryClient();

  // Fetch live comment count to keep stats row synced with CommentSection
  const { data: commentsData } = useQuery({
    queryKey: ['comments', postId],
    queryFn: () => getComments(postId, 0, 100),
    enabled: !!postId, 
  });
  const displayCommentCount = commentsData?.totalElements ?? post?.commentCount ?? 0;

  // Fetch follow status if we have the author's userId
  const { data: followStatus } = useQuery({
    queryKey: ['isFollowing', post?.userId],
    queryFn: () => checkIsFollowing(post!.userId),
    enabled: !!post?.userId && isAuthenticated && user?.userId !== post.userId,
  });

  const isFollowingAuthor = followStatus?.isFollowing || false;

  const followMutation = useMutation({
    mutationFn: () => followUser(post!.userId),
    onSuccess: () => {
      toast.success('팔로우했습니다!');
      queryClient.invalidateQueries({ queryKey: ['isFollowing', post!.userId] });
    }
  });

  const unfollowMutation = useMutation({
    mutationFn: () => unfollowUser(post!.userId),
    onSuccess: () => {
      toast.info('언팔로우했습니다.');
      queryClient.invalidateQueries({ queryKey: ['isFollowing', post!.userId] });
    }
  });

  const handleFollowToggle = () => {
    if (!isAuthenticated) {
      setIsLoginModalOpen(true);
      return;
    }
    if (isFollowingAuthor) {
      setConfirmModal({
        isOpen: true,
        title: '언팔로우 하시겠습니까?',
        description: `${post?.nickname}님의 소식을 더 이상 받지 않게 됩니다.`,
        confirmText: '언팔로우',
        onConfirm: () => {
          unfollowMutation.mutate();
          setConfirmModal(prev => ({ ...prev, isOpen: false }));
        }
      });
    } else {
      followMutation.mutate();
    }
  };

  const isOwner = isAuthenticated && user && (
    user.userId?.trim().toLowerCase() === post?.userId?.trim().toLowerCase() ||
    user.id?.trim().toLowerCase() === post?.userId?.trim().toLowerCase()
  );

  // 메뉴 외부 클릭 시 닫기
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setShowMenu(false);
      }
    }
    if (showMenu) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [showMenu]);

  const handleMouseDown = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!scrollRef.current) return;
    setIsDragging(true);
    setStartX(e.pageX - scrollRef.current.offsetLeft);
    setScrollLeft(scrollRef.current.scrollLeft);
  };

  const handleMouseLeave = () => {
    setIsDragging(false);
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!isDragging || !scrollRef.current) return;
    e.preventDefault();
    const x = e.pageX - scrollRef.current.offsetLeft;
    const walk = (x - startX) * 2;
    scrollRef.current.scrollLeft = scrollLeft - walk;
  };

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
      setIsLoginModalOpen(true);
      return;
    }

    const previousIsLiked = isLiked;
    const previousLikeCount = likeCount;

    setIsLiked(!previousIsLiked);
    setLikeCount(prev => previousIsLiked ? prev - 1 : prev + 1);

    try {
      if (postId !== 'dummy' && !postId.startsWith('dummy')) {
         await toggleLike(postId);
         queryClient.invalidateQueries({ queryKey: ['postLikes', postId] });
      }
    } catch (err) {
      setIsLiked(previousIsLiked);
      setLikeCount(previousLikeCount);
      console.error('좋아요 토글 실패:', err);
    }
  };

  const handleDelete = async () => {
    setShowMenu(false);
    setIsDeleteDialogOpen(true);
  };

  const confirmDeletePost = async () => {
    try {
      await deletePost(postId);
      toast.success('게시물이 삭제되었습니다.');
      router.push('/');
    } catch (err) {
      console.error('삭제 실패:', err);
      toast.error('삭제에 실패했습니다.');
    } finally {
      setIsDeleteDialogOpen(false);
    }
  };

  const handleReport = async (reason: string) => {

    setIsReporting(true);
    try {
      await reportPost(postId as string, reason);
      toast.success('게시물 신고가 접수되었습니다.');
      setIsReportModalOpen(false);
    } catch (err: unknown) {
      const error = err as { response?: { status?: number } };
      if (error?.response?.status === 422) {
        toast.error('이미 신고한 게시물입니다.');
      } else {
        toast.error('신고 접수에 실패했습니다.');
      }
    } finally {
      setIsReporting(false);
    }
  };

  const handleShare = async () => {
    if (!isAuthenticated) {
      setIsLoginModalOpen(true);
      return;
    }
    if (!post) return;
    const url = window.location.href;
    
    // 모바일 환경 판별 (데스크톱에서는 클립보드 복사로 폴백하기 위함)
    const isMobile = typeof navigator !== 'undefined' && /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent);
    
    if (isMobile && navigator.share) {
      try {
        await navigator.share({
          title: `[SIMUL] ${post.nickname}님의 스타일`,
          text: post.caption ? (post.caption.length > 50 ? `${post.caption.slice(0, 50)}...` : post.caption) : '이 코디 어때요?',
          url: url,
        });
      } catch (err) {
        console.log('공유가 취소되었거나 실패했습니다.', err);
      }
    } else {
      // 웹(데스크톱) 환경에서는 즉시 클립보드에 복사
      try {
        await navigator.clipboard.writeText(url);
        toast.success('링크가 복사되었습니다.');
      } catch (err) {
        console.error('클립보드 복사 실패:', err);
        toast.error('링크 복사를 지원하지 않는 브라우저입니다.');
      }
    }
  };

  if (isLoading) return <div className={styles.container}><div style={{padding: '20px', textAlign: 'center'}}>로딩 중...</div></div>;
  if (error) return <div className={styles.container}><div style={{padding: '20px', textAlign: 'center', color: 'red'}}>{error}</div></div>;
  if (!post) return null;

  const dateObj = new Date(post.createdAt);
  const dateStr = `${dateObj.getFullYear()}.${String(dateObj.getMonth() + 1).padStart(2, '0')}.${String(dateObj.getDate()).padStart(2, '0')}`;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button onClick={() => router.back()} className={styles.iconBtn} aria-label="뒤로가기">
          <img src="/icons/arrow-left.png" alt="Back" className={styles.icon} />
        </button>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <button className={styles.iconBtn} aria-label="공유하기" onClick={handleShare}>
            <img src="/icons/square.and.arrow.up.png" alt="Share" className={styles.icon} />
          </button>
          <div className={styles.menuWrapper} ref={menuRef}>
            <button 
              className={styles.iconBtn} 
              onClick={() => setShowMenu(!showMenu)}
              aria-label="더보기"
            >
              <img src="/icons/ellipsis.png" alt="More" className={styles.icon} />
            </button>
            {showMenu && (
              <div className={styles.dropdownMenu}>
                {isOwner ? (
                  <>
                    <button 
                      className={styles.menuItem} 
                      onClick={() => { setShowMenu(false); router.push(`/post/${postId}/edit`); }}
                    >
                      <img src="/icons/pencil.png" alt="" className={styles.menuIcon} />
                      <span>수정하기</span>
                    </button>
                    <div className={styles.menuDivider} />
                    <button 
                      className={`${styles.menuItem} ${styles.menuItemDanger}`} 
                      onClick={handleDelete}
                    >
                      <img src="/icons/trash.png" alt="" className={styles.menuIcon} />
                      <span>삭제하기</span>
                    </button>
                  </>
                ) : (
                  <button 
                    className={`${styles.menuItem} ${styles.menuItemDanger}`}
                    onClick={() => { 
                      setShowMenu(false); 
                      if (!isAuthenticated) {
                        setIsLoginModalOpen(true);
                      } else {
                        setIsReportModalOpen(true); 
                      }
                    }}
                  >
                    <span>🚨 신고하기</span>
                  </button>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      <div className={styles.postDetail}>
        <div className={styles.imageCarouselContainer}>
          <div 
            className={`${styles.imageCarousel} ${isDragging ? styles.dragging : ''}`}
            ref={scrollRef}
            onMouseDown={handleMouseDown}
            onMouseLeave={handleMouseLeave}
            onMouseUp={handleMouseUp}
            onMouseMove={handleMouseMove}
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
                  <img src={url} alt={`게시물 이미지 ${idx + 1}`} className={styles.image} draggable={false} />
                </div>
              ))
            ) : (
              <div className={styles.imageSlide}>
                <img src="/dummy.jpg" alt="기본 이미지" className={styles.image} draggable={false} />
              </div>
            )}
          </div>

          {post.images && post.images.length > 1 && (
            <>
              {currentImgIndex > 0 && (
                <button 
                  className={`${styles.navButton} ${styles.navLeft}`} 
                  onClick={() => {
                    if (scrollRef.current) {
                      const width = scrollRef.current.clientWidth;
                      scrollRef.current.scrollBy({ left: -width, behavior: 'smooth' });
                    }
                  }}
                  aria-label="이전 이미지"
                >
                  <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2.5" fill="none" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="15 18 9 12 15 6"></polyline>
                  </svg>
                </button>
              )}
              {currentImgIndex < post.images.length - 1 && (
                <button 
                  className={`${styles.navButton} ${styles.navRight}`} 
                  onClick={() => {
                    if (scrollRef.current) {
                      const width = scrollRef.current.clientWidth;
                      scrollRef.current.scrollBy({ left: width, behavior: 'smooth' });
                    }
                  }}
                  aria-label="다음 이미지"
                >
                  <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2.5" fill="none" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="9 18 15 12 9 6"></polyline>
                  </svg>
                </button>
              )}
              <div className={styles.carouselIndicators}>
                {post.images.map((_: string, idx: number) => (
                  <div 
                    key={idx} 
                    className={`${styles.indicator} ${currentImgIndex === idx ? styles.indicatorActive : ''}`} 
                  />
                ))}
              </div>
            </>
          )}
        </div>

          {post.isWarned && (
            <div className={styles.warningBanner}>
              <span className={styles.warningIcon}>⚠️</span>
              <span className={styles.warningText}>
                이 게시물은 다수의 신고가 접수되었습니다. 커뮤니티 가이드를 위반한 경우 블라인드 처리될 수 있습니다.
              </span>
            </div>
          )}

        <div className={styles.contentSection}>
          <div className={styles.authorRow}>
            <Link href={`/profile/${post.userId}`} style={{ display: 'flex', alignItems: 'center', gap: '8px', textDecoration: 'none', color: 'inherit' }}>
              {post.profileImageUrl ? (
                 <img src={post.profileImageUrl} alt="아바타" className={styles.authorAvatarImg} style={{width: '36px', height: '36px', borderRadius: '50%', objectFit: 'cover'}} />
              ) : (
                 <span className={styles.authorAvatar}>🧑</span>
              )}
              <div className={styles.authorInfo}>
                <div className={styles.authorName}>{post.nickname}</div>
                <div className={styles.authorMeta}>{dateStr}</div>
              </div>
            </Link>
            
            {(!isAuthenticated || !isOwner) && (
              <button 
                className={styles.followBtn} 
                onClick={handleFollowToggle}
                style={{
                  backgroundColor: isFollowingAuthor ? 'rgba(255,255,255,0.2)' : 'var(--color-primary)',
                  color: '#fff',
                  border: isFollowingAuthor ? '1px solid rgba(255,255,255,0.5)' : 'none',
                  marginLeft: 'auto'
                }}
              >
                {isFollowingAuthor ? '팔로잉 ✓' : '팔로우'}
              </button>
            )}
          </div>

          <p className={styles.caption}>
            {post.caption}
          </p>
          
          {post.tags && post.tags.length > 0 && (
             <div className={styles.tagRow}>
                {post.tags.map((tag: string) => (
                   <Link key={tag} href={`/search?q=%23${tag}&type=tag`} style={{ textDecoration: 'none' }}>
                     <span className={styles.tagChip}>#{tag}</span>
                   </Link>
                ))}
             </div>
          )}

          <div className={styles.stats}>
            <button 
              className={`${styles.statItem} ${isLiked ? styles.liked : ''}`} 
              onClick={handleLike}
              aria-label="좋아요"
            >
              <svg viewBox="0 0 24 24" className={`${styles.statIcon} ${styles.heartIcon}`}>
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              </svg>
            </button>
            <span 
              className={styles.clickableCount} 
              onClick={() => setIsLikeListModalOpen(true)}
              style={{ fontWeight: 600, fontSize: '14px', cursor: 'pointer', marginRight: '16px' }}
            >
              좋아요 {likeCount}개
            </span>
            
            <div className={styles.statItem}>
              <img src="/icons/bubble.png" alt="Comment" className={styles.statIcon} />
              <span>{displayCommentCount}</span>
            </div>
            <div className={styles.statItem} style={{marginLeft: 'auto', color: '#999', fontSize: '13px'}}>
               조회 {post.viewCount}
            </div>
          </div>
          
          <CommentSection 
            postId={postId as string} 
            onLoginRequired={() => setIsLoginModalOpen(true)}
          />
        </div>
      </div>

      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title={confirmModal.title}
        description={confirmModal.description}
        confirmText={confirmModal.confirmText}
        isDanger={true}
        onConfirm={confirmModal.onConfirm}
        onCancel={() => setConfirmModal(prev => ({ ...prev, isOpen: false }))}
      />

      <ReportModal
        isOpen={isReportModalOpen}
        onClose={() => setIsReportModalOpen(false)}
        onSubmit={handleReport}
        isSubmitting={isReporting}
      />

      <DeleteConfirmModal
        isOpen={isDeleteDialogOpen}
        title="게시물을 삭제하시겠습니까?"
        description="삭제된 게시물은 복구할 수 없습니다."
        onConfirm={confirmDeletePost}
        onCancel={() => setIsDeleteDialogOpen(false)}
      />

      <LikeListModal
        isOpen={isLikeListModalOpen}
        onClose={() => setIsLikeListModalOpen(false)}
        postId={postId as string}
      />

      <ConfirmModal
        isOpen={isLoginModalOpen}
        title="로그인이 필요한 서비스입니다"
        description="로그인하고 Simul의 AI 가상시착과 다양한 기능을 자유롭게 이용해 보세요."
        confirmText="로그인하러 가기"
        cancelText="다음에 하기"
        onConfirm={() => router.push('/login')}
        onCancel={() => setIsLoginModalOpen(false)}
      />
    </div>
  );
}
