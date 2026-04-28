'use client';

import { useState } from 'react';
import Button from '@/components/Button';
import styles from './page.module.css';

export default function PostCreatePage() {
  const [tags, setTags] = useState([
    'OOTD', '데일리룩', '봄코디', '가상시착', 'SIMUL',
    '화이트티셔츠', '데님팬츠', '캐주얼', '패션스타그램', '오늘의코디'
  ]);

  const handleDeleteTag = (tagToDelete: string) => {
    setTags(tags.filter(tag => tag !== tagToDelete));
  };

  return (
    <div className={styles.createPage}>
      <header className={styles.header}>
        <h1 className={styles.title}>게시물 작성</h1>
      </header>

      <div className={styles.carouselContainer}>
        <div className={styles.imageScrollArea}>
          {[1, 2, 3, 4].map((id) => (
            <div key={id} className={styles.imageFrame}>
              <img src="/dummy.jpg" alt={`Upload ${id}`} />
            </div>
          ))}
          <div className={styles.addFrame}>
            <span className={styles.plusIcon}>+</span>
          </div>
        </div>
      </div>

      <div className={styles.tagContainer}>
        <div className={styles.tagList}>
          {tags.map((tag, index) => (
            <div key={index} className={styles.tagItem}>
              #{tag}
              <button 
                className={styles.deleteTag} 
                onClick={() => handleDeleteTag(tag)}
                aria-label="태그 삭제"
              >
                ✕
              </button>
            </div>
          ))}
          <button className={styles.addTagBtn} aria-label="태그 추가">
            +
          </button>
        </div>
      </div>

      <div className={styles.formGroup}>
        <div className={styles.textareaWrapper}>
          <textarea
            id="caption"
            className={styles.captionInput}
            placeholder="캡션을 작성해주세요..."
            maxLength={300}
          />
          <div className={styles.captionCounter}>0 / 300</div>
        </div>
      </div>

      <div className={styles.submitRow}>
        <Button variant="secondary" size="lg" fullWidth>취소</Button>
        <Button variant="large-dark" fullWidth>업로드</Button>
      </div>
    </div>
  );
}
