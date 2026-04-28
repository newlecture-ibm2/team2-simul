import Button from '@/components/Button';
import styles from './page.module.css';

export const metadata = {
  title: '게시물 상세 — SIMUL',
  description: 'AI 가상시착 게시물을 확인하세요.',
};

const DUMMY_COMMENTS = [
  { id: 1, author: '수빈', text: '와 이거 너무 잘 어울려요! 어디 옷인가요?', avatar: '🧑' },
  { id: 2, author: '지호', text: '시착 퀄리티 대박이네요 👏', avatar: '👩' },
  { id: 3, author: '예린', text: '저도 이 옷으로 시착해봐야겠어요', avatar: '👧' },
];

export default function PostDetailPage() {
  return (
    <div className={styles.postDetail}>
      <div className={styles.postImage}>
        <div className={styles.postImagePlaceholder}>👗</div>
      </div>

      <button className={styles.tryonBanner}>
        👀 이 게시물의 옷 구경하기 (작성자의 옷장으로 이동)
      </button>

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
        AI 시착으로 미리 확인해보니 구매 고민이 줄었어요.
      </p>

      <div className={styles.actions}>
        <button className={styles.actionBtn}>
          <span className={styles.actionIcon}>♡</span> 24
        </button>
        <button className={styles.actionBtn}>
          <span className={styles.actionIcon}>💬</span> {DUMMY_COMMENTS.length}
        </button>
      </div>

      <div className={styles.commentSection}>
        <h3>댓글</h3>
        {DUMMY_COMMENTS.map((c) => (
          <div key={c.id} className={styles.commentItem}>
            <span className={styles.commentAvatar}>{c.avatar}</span>
            <div className={styles.commentBody}>
              <div className={styles.commentAuthor}>{c.author}</div>
              <div className={styles.commentText}>{c.text}</div>
            </div>
          </div>
        ))}
        <div className={styles.commentInput}>
          <input type="text" placeholder="댓글을 입력하세요..." />
          <Button variant="primary" size="sm">등록</Button>
        </div>
      </div>

      <button className={styles.reportBtn}>🚨 신고</button>
    </div>
  );
}
