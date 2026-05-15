'use client';

import { useState, useEffect, useRef, MouseEvent } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  TouchSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from '@dnd-kit/core';
import {
  SortableContext,
  horizontalListSortingStrategy,
  arrayMove,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

import { getPostDetail, updatePost } from '../../../../../lib/api/feedAPI';
import { analyzeTags } from '../../../../../lib/api/tagAPI';
import { toast } from '@/lib/utils/toast';
import styles from './page.module.css';

export interface PostDetailData {
  postId: string;
  userId: string;
  nickname: string;
  profileImageUrl: string | null;
  images: string[];
  tags: string[];
  imageTagsMap?: Record<string, string[]>;
  manualTags?: string[];
  caption: string;
  likeCount: number;
  viewCount: number;
  isLiked: boolean;
  isPublic: boolean;
  createdAt: string;
}

// 통합 이미지 아이템 (기존/새 이미지를 하나의 배열로 관리)
interface ImageItem {
  id: string;        // 유니크 식별자 (기존: url, 새로운: blob url)
  type: 'existing' | 'new';
  url: string;       // 표시용 URL
  file?: File;       // 새 이미지인 경우 File 객체
}

function SortableImageFrame({
  item,
  onRemove,
}: {
  item: ImageItem;
  onRemove: (id: string) => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: item.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    zIndex: isDragging ? 10 : 0,
    opacity: isDragging ? 0.9 : 1,
    scale: isDragging ? '1.02' : '1',
    boxShadow: isDragging ? '0 10px 20px rgba(0,0,0,0.15)' : 'none',
  };

  return (
    <div ref={setNodeRef} style={style} className={styles.imageFrame}>
      <img src={item.url} alt="이미지" style={{ pointerEvents: 'none' }} />
      <div className={styles.dragHandle} {...attributes} {...listeners}>
        <svg viewBox="0 0 24 24" width="16" height="16" fill="white">
          <path d="M8 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm0 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm0 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm8-12a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm0 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm0 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0z" />
        </svg>
      </div>
      {item.type === 'new' && <span className={styles.newBadge}>NEW</span>}
      <button
        type="button"
        className={styles.removeImageBtn}
        onPointerDown={(e) => e.stopPropagation()}
        onClick={(e) => {
          e.stopPropagation();
          onRemove(item.id);
        }}
        aria-label="이미지 삭제"
      >
        ✕
      </button>
    </div>
  );
}

export default function PostEditPage() {
  const router = useRouter();
  const params = useParams();
  const postId = params.id as string;

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  // 통합 이미지 배열 (기존 + 새 이미지를 하나의 순서 배열로 관리)
  const [imageItems, setImageItems] = useState<ImageItem[]>([]);

  const [manualTags, setManualTags] = useState<string[]>([]);
  const [imageTagsMap, setImageTagsMap] = useState<Record<string, string[]>>({});
  
  const tags = Array.from(new Set([...Object.values(imageTagsMap).flat(), ...manualTags]));
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

  const totalImageCount = imageItems.length;

  // DnD sensors
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 5,
      },
    }),
    useSensor(TouchSensor, {
      activationConstraint: {
        delay: 200,
        tolerance: 5,
      },
    }),
    useSensor(KeyboardSensor)
  );

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (over && active.id !== over.id) {
      setImageItems((items) => {
        const oldIndex = items.findIndex((i) => i.id === active.id);
        const newIndex = items.findIndex((i) => i.id === over.id);
        return arrayMove(items, oldIndex, newIndex);
      });
    }
  };

  useEffect(() => {
    async function loadPost() {
      try {
        const data = await getPostDetail(postId) as PostDetailData;
        
        // 기존 이미지를 ImageItem으로 변환
        const existingItems: ImageItem[] = (data.images || []).map((url) => ({
          id: url,
          type: 'existing' as const,
          url,
        }));
        setImageItems(existingItems);
        
        if (data.imageTagsMap || data.manualTags) {
          setManualTags(data.manualTags || []);
          setImageTagsMap(data.imageTagsMap || {});
        } else {
          setManualTags(data.tags || []);
        }
        
        setCaption(data.caption || '');
        setIsPublic(data.isPublic ?? true);
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
      toast.error('이미지는 최대 5장까지 첨부 가능합니다.');
      return;
    }
    fileInputRef.current?.click();
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const file = e.target.files[0];
      const tempUrl = URL.createObjectURL(file);

      // Vision API 태그 분석
      setIsAnalyzing(true);
      try {
        const result = await analyzeTags(file);
        const extractedTags = result?.recommended_tags;
        if (extractedTags && Array.isArray(extractedTags)) {
          setImageTagsMap(prev => ({
            ...prev,
            [tempUrl]: extractedTags
          }));
        }
      } catch (err: unknown) {
        console.error('태그 분석 실패:', err);
        if (err instanceof Error && err.message.includes('429')) {
          toast.error('사진을 너무 빠르게 많이 올리셨네요! 😅\n잠시만 기다렸다가 다시 올려주시면 자동 태그가 추출됩니다.');
        }
      } finally {
        setIsAnalyzing(false);
      }

      // 통합 배열에 새 이미지 추가
      setImageItems(prev => [...prev, {
        id: tempUrl,
        type: 'new',
        url: tempUrl,
        file,
      }]);
      // 파일 input 초기화
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  // ── 이미지 제거 (통합) ──
  const handleRemoveImage = (id: string) => {
    setImageItems(prev => prev.filter(item => item.id !== id));
    
    setImageTagsMap(prev => {
      const newMap = { ...prev };
      delete newMap[id];
      return newMap;
    });
  };

  // ── 태그 추가/제거 ──
  const handleRemoveTag = (tagToRemove: string) => {
    setManualTags(prev => prev.filter(tag => tag !== tagToRemove));
    
    setImageTagsMap(prev => {
      const newMap = { ...prev };
      let isChanged = false;
      for (const key in newMap) {
        if (newMap[key].includes(tagToRemove)) {
          newMap[key] = newMap[key].filter(tag => tag !== tagToRemove);
          isChanged = true;
        }
      }
      return isChanged ? newMap : prev;
    });
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

      setManualTags(prev => [...prev, newTag]);
      setCustomTag('');
    }
  };

  // ── 저장 ──
  const handleUpdate = async () => {
    if (totalImageCount === 0) {
      toast.error('최소 1장의 이미지가 필요합니다.');
      return;
    }

    if (tags.length > 10) {
      toast.error('태그는 최대 10개까지만 등록 가능합니다. 불필요한 태그를 지워주세요.');
      return;
    }

    // 통합 배열에서 기존/새 이미지를 분리하되 순서를 보존
    const existingUrls = imageItems.filter(i => i.type === 'existing').map(i => i.url);
    const newFiles = imageItems.filter(i => i.type === 'new');

    const existingImageTagsMapToSend: Record<string, string[]> = {};
    const newImageTagsMapToSend: Record<number, string[]> = {};

    existingUrls.forEach(url => {
       if (imageTagsMap[url] && imageTagsMap[url].length > 0) existingImageTagsMapToSend[url] = imageTagsMap[url];
    });
    newFiles.forEach((item, idx) => {
       if (imageTagsMap[item.id] && imageTagsMap[item.id].length > 0) newImageTagsMapToSend[idx] = imageTagsMap[item.id];
    });

    // 전체 순서를 정수 배열로 전달 (기존=url, 새=new:인덱스)
    const imageOrder = imageItems.map(item => {
      if (item.type === 'existing') return item.url;
      return `new:${newFiles.indexOf(item)}`;
    });

    const formData = new FormData();
    formData.append('caption', caption);
    formData.append('isPublic', isPublic ? 'true' : 'false');
    formData.append('existingImageTagsMapJson', JSON.stringify(existingImageTagsMapToSend));
    formData.append('newImageTagsMapJson', JSON.stringify(newImageTagsMapToSend));
    formData.append('imageOrderJson', JSON.stringify(imageOrder));
    manualTags.forEach(tag => formData.append('manualTags', tag));
    existingUrls.forEach(url => formData.append('existingImageUrls', url));
    newFiles.forEach(item => formData.append('newImages', item.file!));

    setIsSaving(true);
    try {
      await updatePost(postId, formData);
      router.replace(`/post/${postId}`);
    } catch (err: unknown) {
      console.error(err);
      toast.error('게시물 수정에 실패했습니다.');
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
        {/* ── 이미지 캐러셀 (드래그 앤 드롭) ── */}
        <div className={styles.carouselContainer}>
          <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
            <div
              className={`${styles.imageScrollArea} ${isDragging ? styles.dragging : ''}`}
              ref={scrollRef}
              onMouseDown={handleMouseDown}
              onMouseLeave={handleMouseLeave}
              onMouseUp={handleMouseUp}
              onMouseMove={handleMouseMove}
              onClickCapture={handleCaptureClick}
            >
              <SortableContext items={imageItems.map(i => i.id)} strategy={horizontalListSortingStrategy}>
                {imageItems.map((item) => (
                  <SortableImageFrame key={item.id} item={item} onRemove={handleRemoveImage} />
                ))}
              </SortableContext>
              
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
          </DndContext>
          {imageItems.length > 1 && (
            <p className={styles.helperText}>드래그 아이콘(⋮⋮)을 잡아 순서를 변경해보세요.</p>
          )}
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
    </div>
  );
}
