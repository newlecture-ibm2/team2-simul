'use client';

import { useEffect, useMemo } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Button from '@/components/Button';
import styles from '../page.module.css';

type Props = {
  className?: string;
  resultImageUrl?: string;
  jobId?: string;
};

export default function ResultClient({ className, resultImageUrl, jobId }: Props) {
  const router = useRouter();
  const mockEnabled = useMemo(() => {
    return process.env.NEXT_PUBLIC_MOCK_TRYON_RESULT === 'true' || process.env.NODE_ENV !== 'production';
  }, []);

  const resultImg = useMemo(() => {
    if (resultImageUrl) return resultImageUrl;
    if (mockEnabled) return '/recent.jpg';
    return null;
  }, [mockEnabled, resultImageUrl]);

  useEffect(() => {
    if (resultImg) return;
    if (!jobId) return;
    if (mockEnabled) return;
    router.replace(`/tryon/processing?job_id=${encodeURIComponent(jobId)}`);
  }, [jobId, mockEnabled, resultImg, router]);

  const handleDownload = async () => {
    if (!resultImg) return;
    try {
      const res = await fetch(resultImg);
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `tryon-${jobId ?? 'result'}.png`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch {
      window.open(resultImg, '_blank', 'noopener,noreferrer');
    }
  };

  return (
    <div className={className}>
      <h1>시착 결과</h1>

      <div className={styles.imageFrame}>
        {resultImg ? (
          <>
            <img src={resultImg} alt="Result" className={styles.mainImage} />
            <div className={styles.viewBadge}>{resultImageUrl ? '결과' : '목업'}</div>
          </>
        ) : (
          <div style={{ padding: 24, color: 'var(--color-text-secondary)', fontSize: 14 }}>
            결과 이미지를 불러올 수 없습니다.
            <br />
            {jobId ? '대기 페이지로 이동 중입니다…' : '시착 생성이 완료된 뒤 다시 시도해주세요.'}
          </div>
        )}
      </div>

      <div className={styles.actions}>
        <div className={styles.actionRow}>
          <Button variant="large-dark" fullWidth onClick={handleDownload} disabled={!resultImg}>
            다운로드
          </Button>
          <Button
            variant="large-dark"
            fullWidth
            onClick={() => alert('준비중입니다.')}
            disabled={!resultImg}
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
    </div>
  );
}
