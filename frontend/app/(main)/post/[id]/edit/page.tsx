'use client';

import { useState, useEffect, useRef, MouseEvent } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getPostDetail, updatePost } from '../../../../../lib/api/feedAPI';
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

export default function PostEditPage() {
  const router = useRouter();
  const params = useParams();
  const postId = params.id as string;

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [imageUrls, setImageUrls] = useState<string[]>([]);
  const [tags, setTags] = useState<string[]>([]);
  const [caption, setCaption] = useState('');
  const [isPublic, setIsPublic] = useState(false);

  const [tagInput, setTagInput] = useState('');
  
  const scrollRef = useRef<HTMLDivElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [startX, setStartX] = useState(0);
  const [scrollLeft, setScrollLeft] = useState(0);

  useEffect(() => {
    async function loadPost() {
      try {
        const data = await getPostDetail(postId) as PostDetailData;
        setImageUrls(data.images || []);
        setTags(data.tags || []);
        setCaption(data.caption || '');
        // Assume isPublic is returned, if not we will just use true/false or fetch it differently
        // MVP: The spec doesn't strictly include isPublic in response, but we can set a default.
        setIsPublic(true); // temporary default if api doesn't provide
        setIsLoading(false);
      } catch (err: any) {
        console.error(err);
        setError('게시물을 불러오는데 실패했습니다.');
        setIsLoading(false);
      }
    }
    loadPost();
  }, [postId]);

  const handleMouseDown = (e: MouseEvent<HTMLDivElement>) => {
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

  const handleMouseMove = (e: MouseEvent<HTMLDivElement>) => {
    if (!isDragging || !scrollRef.current) return;
    e.preventDefault();
    const x = e.pageX - scrollRef.current.offsetLeft;
    const walk = (x - startX) * 2;
    scrollRef.current.scrollLeft = scrollLeft - walk;
  };

  const handleAddTag = () => {
    if (tagInput.trim() === '') return;
    if (tags.length >= 10) {
      alert('태그는 최대 10개까지 추가 가능합니다.');
      return;
    }
    const newTag = tagInput.trim().replace(/^#/, '');
    if (!tags.includes(newTag)) {
      setTags([...tags, newTag]);
    }
    setTagInput('');
  };

  const handleRemoveTag = (tagToRemove: string) => {
    setTags(tags.filter(t => t !== tagToRemove));
  };

  const handleUpdate = async () => {
    try {
      await updatePost(postId, {
        caption,
        isPublic,
        tags
      });
      alert('게시물이 수정되었습니다.');
      router.push(`/post/${postId}`);
    } catch (err: any) {
      console.error(err);
      alert('게시물 수정에 실패했습니다.');
    }
  };

  if (isLoading) return <div className="page-container" style={{ padding: '20px' }}>Loading...</div>;
  if (error) return <div className="page-container" style={{ padding: '20px' }}>{error}</div>;

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <button onClick={() => router.back()} className={styles.cancelBtn}>취소</button>
        <h1 className={styles.title}>게시물 수정</h1>
        <button onClick={handleUpdate} className={styles.submitBtn}>저장</button>
      </header>

      <main className={styles.main}>
        {/* 이미지 확인 섹션 (수정 불가) */}
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>업로드된 이미지</h2>
          </div>
          <div className={styles.imageScrollContainer}>
            <div 
              className={`${styles.imageScroll} ${isDragging ? styles.dragging : ''}`}
              ref={scrollRef}
              onMouseDown={handleMouseDown}
              onMouseLeave={handleMouseLeave}
              onMouseUp={handleMouseUp}
              onMouseMove={handleMouseMove}
            >
              {imageUrls.map((url, idx) => (
                <div key={idx} className={styles.imageFrame}>
                  <img src={url} alt={`업로드된 이미지 ${idx + 1}`} className={styles.previewImage} />
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* 태그 편집 섹션 */}
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>태그 수정 ({tags.length}/10)</h2>
          </div>
          <div className={styles.tagInputContainer}>
            <input 
              type="text" 
              placeholder="# 태그 입력" 
              className={styles.tagInput}
              value={tagInput}
              onChange={(e) => setTagInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAddTag()}
            />
            <button onClick={handleAddTag} className={styles.tagAddBtn}>추가</button>
          </div>
          <div className={styles.tagList}>
            {tags.map((tag, idx) => (
              <div key={idx} className={styles.tagChip}>
                #{tag}
                <button onClick={() => handleRemoveTag(tag)} className={styles.tagRemoveBtn}>✕</button>
              </div>
            ))}
          </div>
        </section>

        {/* 캡션 편집 섹션 */}
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>설명 수정</h2>
          </div>
          <div className={styles.textareaContainer}>
            <textarea 
              className={styles.textarea}
              placeholder="설명을 입력해주세요 (최대 300자)"
              maxLength={300}
              value={caption}
              onChange={(e) => setCaption(e.target.value)}
            />
            <div className={`${styles.charCount} ${caption.length >= 300 ? styles.charCountMax : ''}`}>
              {caption.length}/300
            </div>
          </div>
        </section>

        {/* 공개 여부 설정 */}
        <section className={styles.section}>
          <div className={styles.visibilityToggle}>
            <div>
              <h2 className={styles.sectionTitle}>공개 여부</h2>
              <p className={styles.visibilityDesc}>비공개 시 피드에 노출되지 않습니다.</p>
            </div>
            <button 
              className={`${styles.toggleBtn} ${isPublic ? styles.toggleOn : styles.toggleOff}`}
              onClick={() => setIsPublic(!isPublic)}
            >
              <div className={styles.toggleKnob} />
            </button>
          </div>
        </section>
      </main>
    </div>
  );
}
