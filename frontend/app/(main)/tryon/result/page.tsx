'use client';

import { useState } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import Button from '@/components/Button';
import styles from './page.module.css';

export default function TryonResultPage() {
  const [viewMode, setViewMode] = useState<'result' | 'original'>('result');
  const searchParams = useSearchParams();

  const originalImg = '/dummy.jpg'; // TODO: base image 연동
  const resultImg = searchParams.get('result_image_url') || '/recent.jpg';

  return (
    <div className={styles.resultPage}>
      <h1>시착 결과</h1>

      {/* Main 4:6 Image Frame */}
      <div className={styles.imageFrame}>
        <img 
          src={viewMode === 'result' ? resultImg : originalImg} 
          alt={viewMode === 'result' ? "Result" : "Original"} 
          className={styles.mainImage}
        />
        <div className={styles.viewBadge}>
          {viewMode === 'result' ? '결과' : '원본'}
        </div>

        {/* Thumbnails to toggle view */}
        <div className={styles.thumbnailContainer}>
          <div 
            className={`${styles.thumbnail} ${viewMode === 'original' ? styles.activeThumbnail : ''}`}
            onClick={() => setViewMode('original')}
          >
            <img src={originalImg} alt="Original Thumbnail" />
            <span className={styles.thumbLabel}>원본</span>
          </div>
          <div 
            className={`${styles.thumbnail} ${viewMode === 'result' ? styles.activeThumbnail : ''}`}
            onClick={() => setViewMode('result')}
          >
            <img src={resultImg} alt="Result Thumbnail" />
            <span className={styles.thumbLabel}>결과</span>
          </div>
        </div>
      </div>

      <div className={styles.actions}>
        <div className={styles.actionRow}>
          <Button variant="large-dark" fullWidth>저장하기</Button>
          <Button variant="large-dark" fullWidth>피드에 공유</Button>
        </div>
        <Link href="/tryon/select-clothes">
          <Button variant="large-dark" fullWidth>다른 옷으로 다시 시착</Button>
        </Link>
      </div>
    </div>
  );
}
