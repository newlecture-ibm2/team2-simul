'use client';

import { useMemo, useState } from 'react';
import Link from 'next/link';
import Button from '@/components/Button';
import styles from '../page.module.css';

type Props = {
  className?: string;
  resultImageUrl?: string;
};

export default function ResultClient({ className, resultImageUrl }: Props) {
  const [viewMode, setViewMode] = useState<'result' | 'original'>('result');

  const originalImg = '/dummy.jpg'; // TODO: base image 연동
  const resultImg = useMemo(() => resultImageUrl || '/recent.jpg', [resultImageUrl]);

  return (
    <div className={className}>
      <h1>시착 결과</h1>

      <div className={styles.imageFrame}>
        <img
          src={viewMode === 'result' ? resultImg : originalImg}
          alt={viewMode === 'result' ? 'Result' : 'Original'}
          className={styles.mainImage}
        />
        <div className={styles.viewBadge}>{viewMode === 'result' ? '결과' : '원본'}</div>

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
          <Button variant="large-dark" fullWidth>
            저장하기
          </Button>
          <Button variant="large-dark" fullWidth>
            피드에 공유
          </Button>
        </div>
        <Link href="/tryon/studio">
          <Button variant="large-dark" fullWidth>
            다른 옷으로 다시 시착
          </Button>
        </Link>
      </div>
    </div>
  );
}
