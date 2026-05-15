'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Button from '@/components/Button';
import { getTryonJob } from '@/lib/api/tryonAPI';
import { toast } from '@/lib/utils/toast';
import styles from '../page.module.css';

type Props = {
  className?: string;
  jobId?: string;
  resultImageUrl?: string;
};

export default function ResultClient({ className, jobId, resultImageUrl }: Props) {
  const router = useRouter();
  const [viewMode, setViewMode] = useState<'result' | 'original'>('result');
  const [originalImg, setOriginalImg] = useState<string | null>(null);
  const [resultImgResolved, setResultImgResolved] = useState<string | null>(resultImageUrl ?? null);
  const [isDownloading, setIsDownloading] = useState(false);
  const [showPublishConfirm, setShowPublishConfirm] = useState(false);

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

  const resolveImageUrl = (url: string) => {
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    return new URL(url, window.location.origin).toString();
  };

  const handleDownload = async () => {
    if (!resultImg || isDownloading) return;

    setIsDownloading(true);
    try {
      const response = await fetch(resolveImageUrl(resultImg));
      if (!response.ok) throw new Error('download failed');

      const blob = await response.blob();
      const objectUrl = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = objectUrl;
      link.download = `simul-tryon-${jobId ?? Date.now()}.png`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(objectUrl);
      toast.success('이미지를 저장했습니다.');
    } catch {
      toast.error('이미지 저장에 실패했습니다.');
    } finally {
      setIsDownloading(false);
    }
  };

  const handlePublish = async () => {
    if (!canShowResult) return;
    setShowPublishConfirm(true);
  };

  const convertBlobToDataUrl = (blob: Blob) =>
    new Promise<string>((resolve, reject) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        if (typeof reader.result === 'string') {
          resolve(reader.result);
          return;
        }
        reject(new Error('data url convert failed'));
      };
      reader.onerror = () => reject(reader.error ?? new Error('data url convert failed'));
      reader.readAsDataURL(blob);
    });

  const confirmPublish = async () => {
    if (!canShowResult) return;
    setShowPublishConfirm(false);

    try {
      const response = await fetch(resolveImageUrl(resultImg), { credentials: 'include' });
      if (response.ok) {
        const blob = await response.blob();
        const dataUrl = await convertBlobToDataUrl(blob);
        sessionStorage.setItem('tryon_source_image_data_url', dataUrl);
      }
    } catch {
      sessionStorage.removeItem('tryon_source_image_data_url');
    }

    sessionStorage.setItem('tryon_source_image_url', resultImg);
    const params = new URLSearchParams({ source_image_url: resultImg });
    router.push(`/post/create?${params.toString()}`);
  };

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
          <Button variant="large-dark" fullWidth onClick={handleDownload} disabled={!canShowResult || isDownloading}>
            {isDownloading ? '저장 중...' : '저장하기'}
          </Button>
          <Button
            variant="large-dark"
            fullWidth
            onClick={handlePublish}
            disabled={!canShowResult}
          >
            피드에 공유
          </Button>
        </div>
        <Link href="/tryon/studio">
          <Button variant="large-dark" fullWidth>
            다른 옷으로 다시 시착
          </Button>
        </Link>
      </div>

      {showPublishConfirm && (
        <div className={styles.modalOverlay} role="dialog" aria-modal="true" aria-labelledby="publish-title">
          <div className={styles.modalCard}>
            <h2 id="publish-title">피드에 공유할까요?</h2>
            <p>게시물 작성 페이지에서 캡션과 공개 여부를 직접 설정한 뒤 공유합니다.</p>
            <div className={styles.modalActions}>
              <Button
                variant="large-dark"
                fullWidth
                className={styles.modalButtonCompact}
                onClick={() => setShowPublishConfirm(false)}
              >
                취소
              </Button>
              <Button
                variant="large-dark"
                fullWidth
                className={styles.modalButtonCompact}
                onClick={confirmPublish}
              >
                게시물 작성으로 이동
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
