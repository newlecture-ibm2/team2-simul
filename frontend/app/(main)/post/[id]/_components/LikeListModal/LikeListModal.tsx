import React, { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import { useQuery } from '@tanstack/react-query';
import { getPostLikes, LikeUser } from '@/lib/api/feedAPI';
import Link from 'next/link';
import styles from './LikeListModal.module.css';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  postId: string;
}

export default function LikeListModal({ isOpen, onClose, postId }: Props) {
  const { data, isLoading, error } = useQuery({
    queryKey: ['postLikes', postId],
    queryFn: () => getPostLikes(postId, 0, 100),
    enabled: isOpen,
  });

  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!isOpen || !mounted) return null;

  return createPortal(
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        <div className={styles.header}>
          <h3>좋아요</h3>
          <button className={styles.closeBtn} onClick={onClose}>✕</button>
        </div>
        
        <div className={styles.content}>
          {isLoading ? (
            <div className={styles.loading}>불러오는 중...</div>
          ) : error ? (
            <div className={styles.error}>목록을 불러오지 못했습니다.</div>
          ) : !data || data.content.length === 0 ? (
            <div className={styles.empty}>아직 좋아요가 없습니다.</div>
          ) : (
            <div className={styles.list}>
              {data.content.map((user: LikeUser) => (
                <Link key={user.userId} href={`/profile/${user.userId}`} className={styles.userRow} onClick={onClose}>
                  <div className={styles.avatar}>
                    {user.profileImageUrl ? (
                      <img src={user.profileImageUrl} alt={`${user.nickname}님의 프로필`} />
                    ) : (
                      <span>🧑</span>
                    )}
                  </div>
                  <div className={styles.userInfo}>
                    <span className={styles.nickname}>{user.nickname}</span>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>,
    document.body
  );
}
