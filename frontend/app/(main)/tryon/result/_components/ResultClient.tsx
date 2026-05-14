'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import Button from '@/components/Button';
import { getTryonJob } from '@/lib/api/tryonAPI';
import styles from '../page.module.css';

type Props = {
  className?: string;
  jobId?: string;
  resultImageUrl?: string;
};

export default function ResultClient({ className, jobId, resultImageUrl }: Props) {
  const [viewMode, setViewMode] = useState<'result' | 'original'>('result');
  const [originalImg, setOriginalImg] = useState<string | null>(null);
  const [resultImgResolved, setResultImgResolved] = useState<string | null>(resultImageUrl ?? null);

  useEffect(() => {
    if (!jobId) return;
    let cancelled = false;
    (async () => {
      try {
        const res = await getTryonJob(jobId);
        if (cancelled) return;
        setOriginalImg(res.base_image_url ?? null);
        if (!resultImageUrl) {
          setResultImgResolved(res.result_image_url ?? null);
        }
      } catch {
        // ignore (page can still render result from query string)
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [jobId, resultImageUrl]);

  const resultImg = useMemo(() => resultImgResolved ?? '', [resultImgResolved]);
  const canShowOriginal = Boolean(originalImg);
  const canShowResult = Boolean(resultImg);
  const activeSrc =
    viewMode === 'result'
      ? (canShowResult ? resultImg : '')
      : (canShowOriginal ? (originalImg as string) : '');

  return (
    <div className={className}>
      <h1>시착 결과</h1>

      <div className={styles.imageFrame}>
        {activeSrc ? (
          <img
            src={activeSrc}
            alt={viewMode === 'result' ? 'Result' : 'Original'}
            className={styles.mainImage}
          />
        ) : (
          <div className={styles.mainImage} />
        )}
        <div className={styles.viewBadge}>{viewMode === 'result' ? '결과' : '원본'}</div>

        <div className={styles.thumbnailContainer}>
          <div
            className={`${styles.thumbnail} ${viewMode === 'original' ? styles.activeThumbnail : ''}`}
            onClick={() => canShowOriginal && setViewMode('original')}
            aria-disabled={!canShowOriginal}
          >
            {canShowOriginal ? <img src={originalImg as string} alt="Original Thumbnail" /> : <div />}
            <span className={styles.thumbLabel}>원본</span>
          </div>
          <div
            className={`${styles.thumbnail} ${viewMode === 'result' ? styles.activeThumbnail : ''}`}
            onClick={() => canShowResult && setViewMode('result')}
            aria-disabled={!canShowResult}
          >
            {canShowResult ? <img src={resultImg} alt="Result Thumbnail" /> : <div />}
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
