'use client';

import { useState } from 'react';
import Button from './_components/Button';
import styles from './page.module.css';

export default function PostCreatePage() {

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

      <div className={styles.formGroup}>
        <div className={styles.textareaWrapper}>
          <textarea
            id="caption"
            className={styles.captionInput}
            placeholder="시착 결과에 대해 설명해 주세요..."
            maxLength={300}
          />
          <div className={styles.captionCounter}>0 / 300</div>
        </div>
      </div>

      <div className={styles.submitRow}>
        <Button variant="secondary" size="lg" fullWidth>취소</Button>
        <Button variant="primary" size="lg" fullWidth>업로드</Button>
      </div>
    </div>
  );
}
