'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import Button from '@/components/Button';
import ConfirmModal from '@/components/ConfirmModal/ConfirmModal';
import { getTryonCredits } from '@/lib/api/tryonAPI';
import { getCurrentUser } from '@/lib/api/authAPI';
import { getUserPosts, FeedPost } from '@/lib/api/feedAPI';

import type stylesType from '../../page.module.css';

type CssModule = typeof stylesType;

type Props = {
  styles: CssModule;
};

export default function TryonHomeClient({ styles }: Props) {
  const [credits, setCredits] = useState<{ remaining: number; total_daily: number } | null>(null);
  const [recent, setRecent] = useState<FeedPost[]>([]);
  const [isCreditInfoOpen, setIsCreditInfoOpen] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await getTryonCredits();
        if (cancelled) return;
        // backend: { remaining, total_daily, reset_at }
        setCredits({ remaining: res.remaining, total_daily: res.total_daily });
      } catch {
        // ignore
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const me = await getCurrentUser();
        const posts = await getUserPosts(me.userId, { page: 0, size: 12 });
        if (cancelled) return;
        setRecent((posts.content ?? []).filter((p) => Boolean(p.imageUrl)));
      } catch {
        // ignore
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const creditText = useMemo(() => {
    if (!credits) return '크레딧: - / -';
    return `크레딧: ${credits.remaining} / ${credits.total_daily}`;
  }, [credits]);

  const heroImages = useMemo(() => {
    const imgs = recent
      .map((p) => p.imageUrl)
      .filter((v): v is string => Boolean(v))
      .slice(0, 3);
    return imgs;
  }, [recent]);

  return (
    <div className={styles.tryonHome}>
      <div className={styles.headerRow}>
        <h1 className={styles.title}>가상시착</h1>
        <button type="button" className={styles.creditBadge} onClick={() => setIsCreditInfoOpen(true)}>
          {creditText}
        </button>
      </div>

      <div className={styles.heroSection}>
        <div className={styles.heroCards} aria-hidden>
          <div className={`${styles.heroCard} ${styles.cardLeft}`}>
            {heroImages[0] ? <img src={heroImages[0]} alt="" /> : <div className={styles.imagePlaceholder} />}
          </div>
          <div className={`${styles.heroCard} ${styles.cardCenter}`}>
            {heroImages[1] ? <img src={heroImages[1]} alt="" /> : <div className={styles.imagePlaceholder} />}
          </div>
          <div className={`${styles.heroCard} ${styles.cardRight}`}>
            {heroImages[2] ? <img src={heroImages[2]} alt="" /> : <div className={styles.imagePlaceholder} />}
          </div>
        </div>

        <div className={styles.heroTextContent}>
          <h2 className={styles.heroTitle}>AI로 옷을 미리 입어보세요</h2>
          <p className={styles.heroDesc}>
            내 사진에 원하는 옷을 합성해
            <br />
            가상으로 시착해볼 수 있어요.
            <br />
            구매 전 미리 확인하고 현명한 쇼핑을 시작하세요.
          </p>
          <Link href="/tryon/studio">
            <Button variant="primary" size="lg">
              시착 시작하기
            </Button>
          </Link>
        </div>
      </div>

      <div className={styles.recentSection}>
        <h2>최근 시착 결과</h2>
        <div className={styles.recentScroll}>
          {recent.length === 0 ? (
            <div className={styles.recentEmpty}>아직 시착 결과가 없어요.</div>
          ) : (
            recent.slice(0, 12).map((p) => (
              <Link
                key={p.postId}
                href={`/tryon/result?job_id=${encodeURIComponent(p.postId)}&result_image_url=${encodeURIComponent(p.imageUrl ?? '')}`}
                className={styles.recentItem}
              >
                {p.imageUrl ? (
                  <img src={p.imageUrl} alt="Recent Try-on" className={styles.recentImage} />
                ) : (
                  <div className={styles.imagePlaceholder} />
                )}
              </Link>
            ))
          )}
        </div>
      </div>

      <ConfirmModal
        isOpen={isCreditInfoOpen}
        title="크레딧 안내"
        description={'무료 시착 크레딧은\n매일 24시(KST)에 초기화됩니다.'}
        confirmText="확인"
        cancelText=""
        onConfirm={() => setIsCreditInfoOpen(false)}
        onCancel={() => setIsCreditInfoOpen(false)}
      />
    </div>
  );
}
