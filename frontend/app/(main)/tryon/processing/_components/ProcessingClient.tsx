'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import type stylesType from '../page.module.css';

type CssModule = typeof stylesType;

type Props = {
  className?: string;
  styles: CssModule;
  jobId: string;
};

type TryonStatusEventResponse = {
  job_id: string;
  status: 'processing' | 'completed' | 'failed';
  estimated_seconds_left?: number | null;
  result_image_url?: string | null;
  credit_deducted?: boolean | null;
};

export default function ProcessingClient({ className, styles, jobId }: Props) {
  const router = useRouter();

  const [status, setStatus] = useState<TryonStatusEventResponse['status']>('processing');
  const [estimatedLeft, setEstimatedLeft] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const eventSourceRef = useRef<EventSource | null>(null);

  const sseUrl = useMemo(() => {
    if (!jobId) return null;
    // Must go through BFF (/api) because backend requires Authorization (BFF injects it from session)
    return `/api/tryon/status/${jobId}`;
  }, [jobId]);

  const progressPercent = useMemo(() => {
    if (estimatedLeft == null) return null;
    const total = 20; // backend estimate
    const clampedLeft = Math.min(total, Math.max(0, estimatedLeft));
    return Math.round(((total - clampedLeft) / total) * 100);
  }, [estimatedLeft]);

  useEffect(() => {
    if (!sseUrl) return;
    if (eventSourceRef.current) eventSourceRef.current.close();

    const es = new EventSource(sseUrl);
    eventSourceRef.current = es;

    const onStatus = (e: MessageEvent) => {
      try {
        const payload: TryonStatusEventResponse = JSON.parse(e.data);
        setStatus(payload.status);
        setEstimatedLeft(payload.estimated_seconds_left ?? null);

        if (payload.status === 'completed') {
          es.close();
          const resultImageUrl = payload.result_image_url ?? '';
          const qs = new URLSearchParams();
          qs.set('job_id', payload.job_id);
          if (resultImageUrl) qs.set('result_image_url', resultImageUrl);
          router.replace(`/tryon/result?${qs.toString()}`);
        }

        if (payload.status === 'failed') {
          es.close();
          setErrorMessage('시착 생성에 실패했습니다. 잠시 후 다시 시도해주세요.');
        }
      } catch {
        // ignore parse errors
      }
    };

    const onErrorEvent = (e: MessageEvent) => {
      try {
        // backend sends { error_code, message } as data
        const payload = JSON.parse(e.data) as { message?: string };
        setErrorMessage(payload.message ?? '시착 상태 스트림 연결에 실패했습니다.');
      } catch {
        setErrorMessage('시착 상태 스트림 연결에 실패했습니다.');
      } finally {
        es.close();
      }
    };

    es.addEventListener('processing', onStatus as EventListener);
    es.addEventListener('completed', onStatus as EventListener);
    es.addEventListener('failed', onStatus as EventListener);
    es.addEventListener('error', onErrorEvent as EventListener);

    es.onerror = () => {
      setErrorMessage('시착 상태 스트림 연결이 끊어졌습니다.');
      es.close();
    };

    return () => {
      es.close();
      if (eventSourceRef.current === es) eventSourceRef.current = null;
    };
  }, [router, sseUrl]);

  return (
    <div className={className}>
      <div className={styles.progressSection}>
        <div className={styles.progressBar}>
          <div
            className={styles.progressFill}
            style={
              progressPercent == null
                ? undefined
                : { width: `${progressPercent}%`, animation: 'none' }
            }
          />
        </div>
        <span className={styles.progressText}>
          {jobId ? (
            <>
              결과 생성중... 잠시만 기다려 주세요.
              <br />
              {estimatedLeft != null ? `예상 ${estimatedLeft}초 남음` : '보통 10~30초 정도 소요됩니다.'}
            </>
          ) : (
            <>
              job_id가 없어 시착 상태를 조회할 수 없습니다.
              <br />
              시착 생성 요청 후 이 페이지로 이동해주세요.
            </>
          )}
        </span>

        {errorMessage && (
          <div className={styles.subtitle} style={{ marginTop: 12 }}>
            {errorMessage}
          </div>
        )}

        {!jobId && (
          <div style={{ marginTop: 12 }}>
            <Link href="/tryon/studio" style={{ textDecoration: 'underline' }}>
              시착 스튜디오로 이동
            </Link>
          </div>
        )}
      </div>

      <div className={styles.feedPreview}>
        <div className={styles.frameText}>
          <h2>더 마음에 드는 피드를 골라보세요!</h2>
          <p>
            기다리는 동안 인기 피드를 만나보세요.
            <br />
            선택결과를 반영해 당신의 취향을 추천해드립니다.
          </p>
        </div>
        <div className={`${styles.circle} ${styles.circle1}`}>
          <img src="/dummy.jpg" alt="feed 1" />
        </div>
        <div className={`${styles.circle} ${styles.circle2}`}>
          <img src="/recent.jpg" alt="feed 2" />
        </div>
        <div className={`${styles.circle} ${styles.circle3}`}>
          <img src="/temp.jpg" alt="feed 3" />
        </div>
      </div>
    </div>
  );
}
