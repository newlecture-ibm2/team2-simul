'use client';

import { useState, useEffect, useRef, MouseEvent } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getPostDetail, updatePost } from '../../../../../lib/api/feedAPI';
import { analyzeTags } from '../../../../../lib/api/tagAPI';
import Modal from '../_components/Modal';
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
  const [isSaving, setIsSaving] = useState(false);

  const [modalConfig, setModalConfig] = useState<{
    isOpen: boolean;
    title?: string;
    message: string;
    onConfirm: () => void;
  }>({ isOpen: false, message: '', onConfirm: () => { } });

  const openAlert = (message: string, onConfirm?: () => void) => {
    setModalConfig({
      isOpen: true,
      message,
      onConfirm: () => {
        setModalConfig(prev => ({ ...prev, isOpen: false }));
        if (onConfirm) onConfirm();
      },
    });
  };

  // 기존 서버 이미지 URL + 새로 추가한 로컬 이미지를 구분
  const [existingImageUrls, setExistingImageUrls] = useState<string[]>([]);
  const [newImages, setNewImages] = useState<File[]>([]);
  const [newImageUrls, setNewImageUrls] = useState<string[]>([]);

  const [tags, setTags] = useState<string[]>([]);
  const [customTag, setCustomTag] = useState('');
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [caption, setCaption] = useState('');
  const [isPublic, setIsPublic] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);

  // Drag to scroll state
  const [isDragging, setIsDragging] = useState(false);
  const [hasDragged, setHasDragged] = useState(false);
  const [startX, setStartX] = useState(0);
  const [scrollLeft, setScrollLeft] = useState(0);

  const totalImageCount = existingImageUrls.length + newImages.length;

  useEffect(() => {
    async function loadPost() {
      try {
        const data = await getPostDetail(postId) as PostDetailData;
        setExistingImageUrls(data.images || []);
        setTags(data.tags || []);
        setCaption(data.caption || '');
        setIsPublic(true);
        setIsLoading(false);
      } catch (err: unknown) {
        console.error(err);
        setError('게시물을 불러오는데 실패했습니다.');
        setIsLoading(false);
      }
    }
    loadPost();
  }, [postId]);

  // ── 드래그 스크롤 핸들러 ──
  const handleMouseDown = (e: MouseEvent<HTMLDivElement>) => {
    if (!scrollRef.current) return;
    setIsDragging(true);
    setHasDragged(false);
    setStartX(e.pageX - scrollRef.current.offsetLeft);
    setScrollLeft(scrollRef.current.scrollLeft);
  };

  const handleMouseLeave = () => setIsDragging(false);
  const handleMouseUp = () => setIsDragging(false);

  const handleMouseMove = (e: MouseEvent<HTMLDivElement>) => {
    if (!isDragging || !scrollRef.current) return;
    e.preventDefault();
    const x = e.pageX - scrollRef.current.offsetLeft;
    const walk = (x - startX) * 2;
    if (Math.abs(walk) > 10) setHasDragged(true);
    scrollRef.current.scrollLeft = scrollLeft - walk;
  };

  const handleCaptureClick = (e: MouseEvent) => {
    if (hasDragged) {
      e.stopPropagation();
      e.preventDefault();
    }
  };

  // ── 이미지 추가 ──
  const handleAddImageClick = () => {
    if (totalImageCount >= 5) {
      openAlert('이미지는 최대 5장까지 첨부 가능합니다.');
      return;
    }
    fileInputRef.current?.click();
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const file = e.target.files[0];

      // Vision API 태그 분석
      setIsAnalyzing(true);
      try {
        const result = await analyzeTags(file);
        const extractedTags = result?.recommended_tags;
        if (extractedTags && Array.isArray(extractedTags)) {
          setTags(prev => {
            const newTags = extractedTags.filter((tag: string) => !prev.includes(tag));
            const combined = [...prev, ...newTags];
            return combined;
          });
        }
      } catch (err: unknown) {
        console.error('태그 분석 실패:', err);
        if (err instanceof Error && err.message.includes('429')) {
          openAlert('사진을 너무 빠르게 많이 올리셨네요! 😅\n잠시만 기다렸다가 다시 올려주시면 자동 태그가 추출됩니다.');
        }
      } finally {
        setIsAnalyzing(false);
      }

      setNewImages(prev => [...prev, file]);
      setNewImageUrls(prev => [...prev, URL.createObjectURL(file)]);
      // 파일 input 초기화
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  // ── 이미지 제거 ──
  const handleRemoveExistingImage = (index: number) => {
    setExistingImageUrls(prev => prev.filter((_, i) => i !== index));
  };

  const handleRemoveNewImage = (index: number) => {
    setNewImages(prev => prev.filter((_, i) => i !== index));
    setNewImageUrls(prev => prev.filter((_, i) => i !== index));
  };

  // ── 태그 추가/제거 ──
  const handleRemoveTag = (tagToRemove: string) => {
    setTags(tags.filter(tag => tag !== tagToRemove));
  };

  const handleAddCustomTag = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      const newTag = customTag.trim().toLowerCase().replace(/^#/, '');
      if (!newTag) return;

      if (tags.includes(newTag)) {
        setCustomTag('');
        return;
      }

      setTags([...tags, newTag]);
      setCustomTag('');
    }
  };

  // ── 저장 ──
  const handleUpdate = async () => {
    if (totalImageCount === 0) {
      openAlert('최소 1장의 이미지가 필요합니다.');
      return;
    }

    if (tags.length > 10) {
      openAlert('태그는 최대 10개까지만 등록 가능합니다. 불필요한 태그를 지워주세요.');
      return;
    }
    const formData = new FormData();
    formData.append('caption', caption);
    formData.append('isPublic', isPublic ? 'true' : 'false');
    tags.forEach(tag => formData.append('tags', tag));
    existingImageUrls.forEach(url => formData.append('existingImageUrls', url));
    newImages.forEach(img => formData.append('newImages', img));

    setIsSaving(true);
    try {
      await updatePost(postId, formData);
      router.replace(`/post/${postId}`);
    } catch (err: unknown) {
      console.error(err);
      openAlert('게시물 수정에 실패했습니다.');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) return <div className={styles.container}><div style={{ padding: '40px', textAlign: 'center' }}>로딩 중...</div></div>;
  if (error) return <div className={styles.container}><div style={{ padding: '40px', textAlign: 'center', color: 'red' }}>{error}</div></div>;

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <button onClick={() => router.back()} className={styles.cancelBtn}>취소</button>
        <h1 className={styles.title}>게시물 수정</h1>
        <button
          onClick={handleUpdate}
          className={styles.submitBtn}
          disabled={isSaving}
        >
          {isSaving ? '저장 중...' : '저장'}
        </button>
      </header>

      <main className={styles.main}>
        {/* ── 이미지 캐러셀 (생성 페이지와 동일한 스타일) ── */}
        <div className={styles.carouselContainer}>
          <div
            className={`${styles.imageScrollArea} ${isDragging ? styles.dragging : ''}`}
            ref={scrollRef}
            onMouseDown={handleMouseDown}
            onMouseLeave={handleMouseLeave}
            onMouseUp={handleMouseUp}
            onMouseMove={handleMouseMove}
            onClickCapture={handleCaptureClick}
          >
            {/* 기존 서버 이미지 */}
            {existingImageUrls.map((url, index) => (
              <div key={`existing-${index}`} className={styles.imageFrame}>
                <img src={url} alt={`기존 이미지 ${index + 1}`} />
                <button
                  className={styles.removeImageBtn}
                  onClick={() => handleRemoveExistingImage(index)}
                  aria-label="이미지 삭제"
                >
                  ✕
                </button>
              </div>
            ))}
            {/* 새로 추가된 이미지 */}
            {newImageUrls.map((url, index) => (
              <div key={`new-${index}`} className={styles.imageFrame}>
                <img src={url} alt={`새 이미지 ${index + 1}`} />
                <span className={styles.newBadge}>NEW</span>
                <button
                  className={styles.removeImageBtn}
                  onClick={() => handleRemoveNewImage(index)}
                  aria-label="새 이미지 삭제"
                >
                  ✕
                </button>
              </div>
            ))}
            {/* 추가 버튼 */}
            {totalImageCount < 5 && (
              <div className={styles.addFrame} onClick={handleAddImageClick}>
                <span className={styles.plusIcon}>+</span>
                <input
                  type="file"
                  ref={fileInputRef}
                  hidden
                  accept="image/jpeg, image/png, image/webp"
                  onChange={handleFileChange}
                />
              </div>
            )}
          </div>
        </div>

        {/* ── 태그 선택 (생성 페이지와 동일) ── */}
        <div className={styles.tagContainer}>
          <div className={styles.tagHeader}>
            <h2 className={styles.sectionTitle}>태그</h2>
            <span className={`${styles.tagCount} ${tags.length > 10 ? styles.tagCountError : ''}`}>
              {tags.length} / 10
            </span>
          </div>
          {isAnalyzing && (
            <div className={styles.analyzingText}>
              <span className={styles.analyzingDot} />
              자동 태그 분석 중...
            </div>
          )}
          <div className={styles.tagList}>
            {tags.map((tag, index) => (
              <div key={index} className={styles.tagItem}>
                #{tag}
                <button
                  type="button"
                  className={styles.removeTagBtn}
                  onClick={() => handleRemoveTag(tag)}
                  aria-label={`태그 ${tag} 삭제`}
                >
                  ✕
                </button>
              </div>
            ))}
            <input
              type="text"
              className={styles.tagInput}
              placeholder="태그 입력 후 Enter"
              value={customTag}
              onChange={(e) => setCustomTag(e.target.value)}
              onKeyDown={handleAddCustomTag}
            />
          </div>
        </div>

        {/* ── 캡션 입력 ── */}
        <div className={styles.formGroup}>
          <div className={styles.textareaWrapper}>
            <textarea
              id="caption"
              className={styles.captionInput}
              placeholder="캡션을 작성해주세요..."
              maxLength={300}
              value={caption}
              onChange={(e) => setCaption(e.target.value)}
            />
            <div className={`${styles.captionCounter} ${caption.length >= 300 ? styles.captionCounterMax : ''}`}>
              {caption.length} / 300
            </div>
          </div>
        </div>

        {/* ── 공개 토글 ── */}
        <div className={styles.publicToggleRow}>
          <div className={styles.toggleTextContainer}>
            <span className={styles.toggleLabel}>커뮤니티 피드에 공유하기</span>
            <span className={styles.toggleSubLabel}>(체크 해제 시 내 프로필에서 나만 보기)</span>
          </div>
          <label className={styles.toggleSwitch}>
            <input
              type="checkbox"
              checked={isPublic}
              onChange={(e) => setIsPublic(e.target.checked)}
            />
            <span className={styles.toggleSlider} />
          </label>
        </div>
      </main>
      <Modal {...modalConfig} />
    </div>
  );
}
