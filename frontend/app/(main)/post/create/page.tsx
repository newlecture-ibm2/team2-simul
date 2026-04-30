'use client';

import { useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import Button from './_components/Button';
import { createPost } from '@/lib/api/feedAPI';
import { analyzeTags } from '@/lib/api/tagAPI';
import styles from './page.module.css';

export default function PostCreatePage() {
  const router = useRouter();

  const [images, setImages] = useState<File[]>([]);
  const [imageUrls, setImageUrls] = useState<string[]>([]);
  const [tags, setTags] = useState<string[]>([]);
  const [caption, setCaption] = useState('');
  const [isPublic, setIsPublic] = useState(false);
  const [suggestedTags, setSuggestedTags] = useState<string[]>([]);
  const [isAnalyzing, setIsAnalyzing] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleAddImageClick = () => {
    if (images.length >= 5) {
      alert('이미지는 최대 5장까지 첨부 가능합니다.');
      return;
    }
    fileInputRef.current?.click();
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const file = e.target.files[0];

      setIsAnalyzing(true);
      try {
        const result = await analyzeTags(file);
        const extractedTags = result?.recommended_tags;
        if (extractedTags && Array.isArray(extractedTags)) {
          setSuggestedTags(prev => Array.from(new Set([...prev, ...extractedTags])));
        }
      } catch (err) {
        console.error('태그 분석 실패:', err);
      } finally {
        setIsAnalyzing(false);
      }

      setImages([...images, file]);
      setImageUrls([...imageUrls, URL.createObjectURL(file)]);
    }
  };

  const handleRemoveImage = (index: number) => {
    setImages(images.filter((_, i) => i !== index));
    setImageUrls(imageUrls.filter((_, i) => i !== index));
  };

  const handleToggleTag = (tagToToggle: string) => {
    if (tags.includes(tagToToggle)) {
      setTags(tags.filter(tag => tag !== tagToToggle));
    } else {
      if (tags.length >= 10) {
        alert('태그는 최대 10개까지만 선택 가능합니다.');
        return;
      }
      setTags([...tags, tagToToggle]);
    }
  };

  const handleSubmit = async () => {
    if (images.length === 0) {
      alert('최소 1장의 이미지를 첨부해주세요.');
      return;
    }

    const formData = new FormData();
    images.forEach(img => formData.append('images', img));
    formData.append('caption', caption);
    formData.append('isPublic', isPublic ? 'true' : 'false');
    tags.forEach(tag => formData.append('tags', tag));

    try {
      await createPost(formData);
      alert('게시물이 성공적으로 작성되었습니다.');
      router.push('/');
    } catch (err) {
      console.error('게시물 작성 실패:', err);
      alert('게시물 작성에 실패했습니다.');
    }
  };

  return (
    <div className={styles.createPage}>
      <header className={styles.header}>
        <h1 className={styles.title}>게시물 작성</h1>
      </header>

      {/* Image Carousel */}
      <div className={styles.carouselContainer}>
        <div className={styles.imageScrollArea}>
          {imageUrls.map((url, index) => (
            <div key={index} className={styles.imageFrame}>
              <img src={url} alt={`Upload ${index + 1}`} />
              <button
                className={styles.removeImageBtn}
                onClick={() => handleRemoveImage(index)}
                aria-label="이미지 삭제"
              >
                ✕
              </button>
            </div>
          ))}
          {images.length < 5 && (
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

      {/* Tags */}
      <div className={styles.tagContainer}>
        {isAnalyzing && (
          <div className={styles.analyzingText}>
            <span className={styles.analyzingDot} />
            자동 태그 분석 중...
          </div>
        )}
        <div className={styles.tagList}>
          {suggestedTags.map((tag, index) => {
            const isSelected = tags.includes(tag);
            return (
              <button
                key={index}
                className={`${styles.tagItem} ${isSelected ? styles.tagItemSelected : ''}`}
                onClick={() => handleToggleTag(tag)}
                aria-label={`태그 ${tag} ${isSelected ? '해제' : '선택'}`}
              >
                #{tag}
              </button>
            );
          })}
        </div>
        <div className={styles.tagCountText}>
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
        <label className={styles.toggleSwitch}>
          <input
            type="checkbox"
            checked={isPublic}
            onChange={(e) => setIsPublic(e.target.checked)}
          />
          <span className={styles.toggleSlider} />
        </label>
        <span className={styles.toggleLabel}>전체 공개</span>
      </div>

      {/* Submit */}
      <div className={styles.submitRow}>
        <Button variant="secondary" size="lg" fullWidth onClick={() => router.back()}>취소</Button>
        <Button variant="large-dark" fullWidth onClick={handleSubmit}>업로드</Button>
      </div>
    </div>
  );
}
