'use client';

import { useState, useRef, MouseEvent, useEffect, useSearchParams } from 'react';
import { useRouter } from 'next/navigation';
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

import Button from './_components/Button';
import { createPost } from '@/lib/api/feedAPI';
import { analyzeTags } from '@/lib/api/tagAPI';
import { toast } from '@/lib/utils/toast';
import styles from './page.module.css';

function SortableImageFrame({ url, index, onRemove }: { url: string; index: number; onRemove: (index: number) => void }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: url });

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
      <img src={url} alt={`Upload ${index + 1}`} style={{ pointerEvents: 'none' }} />
      <div className={styles.dragHandle} {...attributes} {...listeners}>
        <svg viewBox="0 0 24 24" width="16" height="16" fill="white">
          <path d="M8 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm0 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm0 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm8-12a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm0 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm0 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0z" />
        </svg>
      </div>
      <button
        type="button"
        className={styles.removeImageBtn}
        onPointerDown={(e) => e.stopPropagation()}
        onClick={(e) => {
          e.stopPropagation();
          onRemove(index);
        }}
        aria-label="이미지 삭제"
      >
        ✕
      </button>
    </div>
  );
}


export default function PostCreatePage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const sourceImageUrl = searchParams.get('source_image_url');

  const [images, setImages] = useState<File[]>([]);
  const [imageUrls, setImageUrls] = useState<string[]>([]);
  
  // Tag States
  const [manualTags, setManualTags] = useState<string[]>([]);
  const [imageTagsMap, setImageTagsMap] = useState<Record<string, string[]>>({});
  
  // Derived Tags (combines all unique tags)
  const tags = Array.from(new Set([...Object.values(imageTagsMap).flat(), ...manualTags]));
  const [caption, setCaption] = useState('');
  const [isPublic, setIsPublic] = useState(false);
  const [customTag, setCustomTag] = useState('');
  const [isAnalyzing, setIsAnalyzing] = useState(false);



  const fileInputRef = useRef<HTMLInputElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);
  const loadingSourceImageRef = useRef<string | null>(null);

  // Drag to scroll state
  const [isDragging, setIsDragging] = useState(false);
  const [hasDragged, setHasDragged] = useState(false);
  const [startX, setStartX] = useState(0);
  const [scrollLeft, setScrollLeft] = useState(0);

  const resolveImageUrl = (url: string) => {
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    return new URL(url, window.location.origin).toString();
  };

  const previewSourceImageUrl = sourceImageUrl ? resolveImageUrl(sourceImageUrl) : '';
  const displayImageUrls = imageUrls.length > 0
    ? imageUrls
    : (previewSourceImageUrl ? [previewSourceImageUrl] : []);

  useEffect(() => {
    const queryImageUrl = new URLSearchParams(window.location.search).get('source_image_url');
    const storedImageDataUrl = sessionStorage.getItem('tryon_source_image_data_url');
    const storedImageUrl = sessionStorage.getItem('tryon_source_image_url');
    const candidateImageUrl = sourceImageUrl || queryImageUrl || storedImageUrl;
    const previewUrl = storedImageDataUrl || (candidateImageUrl ? resolveImageUrl(candidateImageUrl) : null);
    if (previewUrl && imageUrls.length === 0) {
      setImageUrls([previewUrl]);
    }

    const loadingKey = storedImageDataUrl || candidateImageUrl;
    if ((!storedImageDataUrl && !candidateImageUrl) || images.length > 0) return;
    if (loadingKey && loadingSourceImageRef.current === loadingKey) return;

    loadingSourceImageRef.current = loadingKey;
    let cancelled = false;

    (async () => {
      try {
        if (storedImageDataUrl) {
          const response = await fetch(storedImageDataUrl);
          if (!response.ok) throw new Error('source image data fetch failed');
          const blob = await response.blob();
          const file = new File([blob], 'tryon-result.png', { type: blob.type || 'image/png' });
          if (cancelled) return;

          setImages([file]);
          setImageUrls([URL.createObjectURL(file)]);
          sessionStorage.removeItem('tryon_source_image_data_url');
          sessionStorage.removeItem('tryon_source_image_url');
          return;
        }

        if (!candidateImageUrl) throw new Error('source image url missing');
        let blob: Blob | null = null;
        const requestUrl = resolveImageUrl(candidateImageUrl);

        for (let attempt = 0; attempt < 3; attempt += 1) {
          const response = await fetch(requestUrl, { credentials: 'include' });
          if (response.ok) {
            blob = await response.blob();
            break;
          }
          await new Promise((resolve) => setTimeout(resolve, 300));
        }

        if (!blob) throw new Error('source image fetch failed');
        const file = new File([blob], 'tryon-result.png', { type: blob.type || 'image/png' });
        if (cancelled) return;

        setImages([file]);
        setImageUrls([URL.createObjectURL(file)]);
        sessionStorage.removeItem('tryon_source_image_data_url');
        sessionStorage.removeItem('tryon_source_image_url');
      } catch {
        loadingSourceImageRef.current = null;
        toast.error('시착 결과 이미지를 불러오지 못했습니다.');
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [sourceImageUrl, images.length, imageUrls.length]);
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
      const oldIndex = imageUrls.indexOf(active.id as string);
      const newIndex = imageUrls.indexOf(over.id as string);

      setImageUrls((items) => arrayMove(items, oldIndex, newIndex));
      setImages((items) => arrayMove(items, oldIndex, newIndex));
    }
  };

  const handleMouseDown = (e: MouseEvent<HTMLDivElement>) => {
    if (!scrollRef.current) return;
    setIsDragging(true);
    setHasDragged(false);
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
    const walk = (x - startX) * 2; // 스크롤 속도

    if (Math.abs(walk) > 10) {
      setHasDragged(true); // 실제 스크롤 발생
    }

    scrollRef.current.scrollLeft = scrollLeft - walk;
  };

  const handleCaptureClick = (e: MouseEvent) => {
    if (hasDragged) {
      e.stopPropagation();
      e.preventDefault();
    }
  };

  const handleAddImageClick = () => {
    if (displayImageUrls.length >= 5) {
      toast.error('이미지는 최대 5장까지 첨부 가능합니다.');
      return;
    }
    fileInputRef.current?.click();
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const file = e.target.files[0];
      const tempUrl = URL.createObjectURL(file);

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

      setImages(prev => [...prev, file]);
      setImageUrls(prev => [...prev, tempUrl]);
    }
  };

  const handleRemoveImage = (index: number) => {
    const urlToRemove = imageUrls[index];
    setImages(images.filter((_, i) => i !== index));
    setImageUrls(imageUrls.filter((_, i) => i !== index));
    
    // Remove tags associated with this image
    setImageTagsMap(prev => {
      const newMap = { ...prev };
      delete newMap[urlToRemove];
      return newMap;
    });
  };

  const handleRemoveTag = (tagToRemove: string) => {
    // Remove from manual tags
    setManualTags(prev => prev.filter(tag => tag !== tagToRemove));
    
    // Remove from all image tags maps
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
        return;
      }
      setManualTags([...manualTags, newTag]);
      setCustomTag('');
    }
  };

  const handleSubmit = async () => {
    let submitImages = images;
    if (submitImages.length === 0 && displayImageUrls.length > 0) {
      try {
        const response = await fetch(displayImageUrls[0], { credentials: 'include' });
        if (!response.ok) throw new Error('image fetch failed');
        const blob = await response.blob();
        submitImages = [new File([blob], 'tryon-result.png', { type: blob.type || 'image/png' })];
        setImages(submitImages);
      } catch {
        toast.error('생성 이미지를 불러오지 못했습니다. 다시 시도해주세요.');
        return;
      }
    }

    if (submitImages.length === 0) {
      toast.error('최소 1장의 이미지를 첨부해주세요.');
      return;
    }

    if (tags.length > 10) {
      toast.error('태그는 최대 10개까지만 등록 가능합니다. 불필요한 태그를 지워주세요.');
      return;
    }

    const formData = new FormData();
    submitImages.forEach(img => formData.append('images', img));
    formData.append('caption', caption);
    formData.append('isPublic', isPublic ? 'true' : 'false');
    
    const newImageTagsMapToSend: Record<number, string[]> = {};
    imageUrls.forEach((url, idx) => {
      if (imageTagsMap[url] && imageTagsMap[url].length > 0) {
        newImageTagsMapToSend[idx] = imageTagsMap[url];
      }
    });
    formData.append('newImageTagsMapJson', JSON.stringify(newImageTagsMapToSend));
    manualTags.forEach(tag => formData.append('manualTags', tag));

    try {
      await createPost(formData);
      toast.success('게시물이 성공적으로 작성되었습니다.');
      router.push('/');
    } catch (err) {
      console.error('게시물 작성 실패:', err);
      toast.error('게시물 작성에 실패했습니다.');
    }
  };

  return (
    <div className={styles.createPage}>
      <header className={styles.header}>
        <h1 className={styles.title}>게시물 작성</h1>
      </header>

      {/* Image Carousel */}
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
              <SortableContext items={displayImageUrls} strategy={horizontalListSortingStrategy}>
            {displayImageUrls.map((url, index) => (
  <SortableImageFrame key={url} url={url} index={index} onRemove={handleRemoveImage} />
))}
            </SortableContext>
            
            {displayImageUrls.length < 5 && (
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
        {displayImageUrls.length > 1 && (
          <p className={styles.helperText}>드래그 아이콘(⋮⋮)을 잡아 순서를 변경해보세요.</p>
        )}
      </div>

      {/* Tags */}
      <div className={styles.tagContainer}>
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
        <div className={`${styles.tagCountText} ${tags.length > 10 ? styles.tagCountError : ''}`}>
          선택된 태그: {tags.length} / 10
        </div>
      </div>

      {/* Caption */}
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
          <div className={styles.captionCounter}>{caption.length} / 300</div>
        </div>
      </div>

      {/* Public Toggle */}
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

      {/* Submit */}
      <div className={styles.submitRow}>
        <Button variant="secondary" size="lg" fullWidth onClick={() => router.back()}>취소</Button>
        <Button variant="large-dark" fullWidth onClick={handleSubmit}>업로드</Button>
      </div>
    </div>
  );
}
