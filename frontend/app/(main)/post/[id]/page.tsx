'use client';

import { useState } from 'react';
import { useParams } from 'next/navigation';
import Button from '@/components/Button';
import Link from 'next/link';
import { toggleLike } from '../../../../lib/api/feedAPI';
import { useAuthStore } from '../../../../lib/stores/useAuthStore';
import styles from './page.module.css';

const DUMMY_COMMENTS = [
  { id: 1, author: '수빈', text: '와 이거 너무 잘 어울려요! 어디 옷인가요?', avatar: '🧑' },
  { id: 2, author: '지호', text: '시착 퀄리티 대박이네요 👏', avatar: '👩' },
  { id: 3, author: '예린', text: '저도 이 옷으로 시착해봐야겠어요', avatar: '👧' },
];

export default function PostDetailPage() {
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(24);
  const params = useParams();
  const postId = params.id as string;
  const { isAuthenticated } = useAuthStore();

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
    } catch (error) {
      setIsLiked(previousIsLiked);
      setLikeCount(previousLikeCount);
      console.error('좋아요 토글 실패:', error);
    }
  };

  return (
    <div className={styles.container}>
      {/* Top Header Icons Overlay */}
      <div className={styles.header}>
        <Link href="/" className={styles.iconBtn} aria-label="뒤로가기">
          <img src="/icons/arrow-left.png" alt="Back" className={styles.icon} />
        </Link>
        <button className={styles.iconBtn} aria-label="공유하기">
          <img src="/icons/square.and.arrow.up.png" alt="Share" className={styles.icon} />
        </button>
      </div>

      <div className={styles.postDetail}>
        <div className={styles.postImage}>
          <img src="/dummy.jpg" alt="게시물 이미지" className={styles.image} />
        </div>

        <div className={styles.contentSection}>
          <div className={styles.authorRow}>
            <span className={styles.authorAvatar}>🧑</span>
            <div className={styles.authorInfo}>
              <div className={styles.authorName}>지수</div>
              <div className={styles.authorMeta}>2시간 전</div>
            </div>
            <button className={styles.followBtn}>팔로우</button>
          </div>

          <p className={styles.caption}>
            오늘 시착해본 봄 코디! 🌸 데님 자켓에 화이트 티 조합이 깔끔하게 떨어지네요.
            AI 시착으로 미리 확인해보니 구매 고민이 줄었어요. 마음에 쏙 드는 실루엣입니다.
          </p>

          <div className={styles.stats}>
            <button className={styles.statItem} onClick={handleLike}>
              <img 
                src={isLiked ? "/icons/heart-filled.png" : "/icons/heart.png"} 
                alt="Like" 
                className={styles.statIcon} 
                onError={(e) => {
                   // Fallback if heart-filled doesn't exist
                   (e.target as HTMLImageElement).src = "/icons/heart.png";
                }}
              />
              <span>{likeCount}</span>
            </button>
            <div className={styles.statItem}>
              <img src="/icons/bubble.png" alt="Comment" className={styles.statIcon} />
              <span>{DUMMY_COMMENTS.length}</span>
            </div>
            <button className={styles.statItem}>
              <img src="/icons/paperplane.png" alt="Share" className={styles.statIcon} />
            </button>
          </div>

          <div className={styles.commentSection}>
            <div className={styles.commentList}>
              {DUMMY_COMMENTS.map((c) => (
                <div key={c.id} className={styles.commentItem}>
                  <span className={styles.commentAvatar}>{c.avatar}</span>
                  <div className={styles.commentBody}>
                    <div className={styles.commentAuthor}>{c.author}</div>
                    <div className={styles.commentText}>{c.text}</div>
                  </div>
                </div>
              ))}
            </div>

            <div className={styles.commentInput}>
              <input type="text" placeholder="댓글을 입력하세요..." />
              <Button variant="primary" size="sm">게시</Button>
            </div>
          </div>
          
          <button className={styles.reportBtn}>🚨 게시물 신고하기</button>
        </div>
      </div>
    </div>
  );
}
