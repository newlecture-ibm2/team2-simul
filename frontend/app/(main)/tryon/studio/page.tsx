'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Button from '@/components/Button';
import ConfirmModal from '@/components/ConfirmModal/ConfirmModal';
import Toggle from './_components/Toggle/Toggle';
import styles from './page.module.css';
import { deleteBaseImage, generateTryon, getMyBaseImages, getTryonCredits, uploadBaseImage } from '@/lib/api/tryonAPI';
import { addClosetItem, deleteClosetItem, getClosetItems } from '@/lib/api/closetAPI';

type ClosetItemSummary = {
  itemId: string;
  imageUrl: string;
  category: string | null;
};

type ClothesCategoryUi = 'top' | 'bottom' | 'accessory';
type ClothesCategoryBackend = 'TOP' | 'BOTTOM' | 'ACCESSORY';
type SelectedCloth = { itemId: string; imageUrl: string };

export default function StudioPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<'person' | 'clothes'>('clothes');
  const [clothesCategory, setClothesCategory] = useState<ClothesCategoryUi>('top');
  const [baseImages, setBaseImages] = useState<Array<{ base_image_id: string; image_url: string }>>([]);
  const [selectedBaseImageId, setSelectedBaseImageId] = useState<string>('');
  const [closetItems, setClosetItems] = useState<ClosetItemSummary[]>([]);
  const [selectedClothesByCategory, setSelectedClothesByCategory] = useState<
    Partial<Record<ClothesCategoryBackend, SelectedCloth>>
  >({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isUploadingBaseImage, setIsUploadingBaseImage] = useState(false);
  const [isUploadingClothes, setIsUploadingClothes] = useState(false);
  const [remainingCredits, setRemainingCredits] = useState<number | null>(null);
  const [isCreditExhaustedModalOpen, setIsCreditExhaustedModalOpen] = useState(false);
  const baseImageInputRef = useRef<HTMLInputElement | null>(null);
  const clothesImageInputRef = useRef<HTMLInputElement | null>(null);

  const toBackendCategory = (ui: ClothesCategoryUi): ClothesCategoryBackend => {
    if (ui === 'top') return 'TOP';
    if (ui === 'bottom') return 'BOTTOM';
    return 'ACCESSORY';
  };

  const clothesCategoryLabel = (ui: ClothesCategoryUi) => {
    if (ui === 'top') return '상의';
    if (ui === 'bottom') return '하의';
    return '악세서리';
  };

  const selectedPersonImageUrl = useMemo(() => {
    const picked = baseImages.find(b => b.base_image_id === selectedBaseImageId);
    return picked?.image_url ?? '';
  }, [baseImages, selectedBaseImageId]);

  const refreshBaseImages = async () => {
    const res = await getMyBaseImages();
    const list = (res.base_images ?? []).map(b => ({
      base_image_id: b.base_image_id,
      image_url: b.image_url,
    }));
    setBaseImages(list);
    if (!selectedBaseImageId && list.length > 0) {
      setSelectedBaseImageId(list[0].base_image_id);
    }
  };

  const refreshClosetItems = async () => {
    const category = toBackendCategory(clothesCategory);
    const res = await getClosetItems({ category, sort: 'recent', page: 0, size: 20 });
    setClosetItems(
      (res.items ?? []).map((it) => ({
        itemId: it.itemId,
        imageUrl: it.imageUrl,
        category: it.category,
      }))
    );
  };

  useEffect(() => {
    let canceled = false;
    (async () => {
      try {
        const res = await getMyBaseImages();
        const list = (res.base_images ?? []).map(b => ({
          base_image_id: b.base_image_id,
          image_url: b.image_url,
        }));
        if (canceled) return;
        setBaseImages(list);
        if (!selectedBaseImageId && list.length > 0) {
          setSelectedBaseImageId(list[0].base_image_id);
        }
      } catch {
        // ignore (toast will show from apiClient)
      }
    })();
    return () => {
      canceled = true;
    };
  }, [selectedBaseImageId]);

  useEffect(() => {
    let canceled = false;
    (async () => {
      try {
        const category = toBackendCategory(clothesCategory);
        const res = await getClosetItems({ category, sort: 'recent', page: 0, size: 20 });
        if (canceled) return;
        setClosetItems(
          (res.items ?? []).map((it) => ({
            itemId: it.itemId,
            imageUrl: it.imageUrl,
            category: it.category,
          }))
        );
      } catch {
        // ignore
      }
    })();
    return () => {
      canceled = true;
    };
  }, [clothesCategory]);

  useEffect(() => {
    let canceled = false;
    (async () => {
      try {
        const res = await getTryonCredits();
        if (canceled) return;
        setRemainingCredits(res.remaining);
      } catch {
        // ignore
      }
    })();
    return () => {
      canceled = true;
    };
  }, []);

  const handleToggleCloth = (cloth: ClosetItemSummary) => {
    const category = toBackendCategory(clothesCategory);
    setSelectedClothesByCategory((prev) => {
      const current = prev[category];
      if (current?.itemId === cloth.itemId) {
        const { [category]: _, ...rest } = prev;
        return rest;
      }
      return { ...prev, [category]: { itemId: cloth.itemId, imageUrl: cloth.imageUrl } };
    });
  };

  const handleUploadClick = () => {
    if (activeTab === 'person') {
      baseImageInputRef.current?.click();
      return;
    }

    clothesImageInputRef.current?.click();
  };

  const handleBaseImagePicked = async (file: File | null) => {
    if (!file) return;
    setIsUploadingBaseImage(true);
    try {
      const uploaded = await uploadBaseImage(file);
      await refreshBaseImages();
      setSelectedBaseImageId(uploaded.base_image_id);
      setActiveTab('person');
    } finally {
      setIsUploadingBaseImage(false);
      if (baseImageInputRef.current) baseImageInputRef.current.value = '';
    }
  };

  const handleDeleteBaseImage = async (baseImageId: string) => {
    if (!confirm('이 베이스 이미지를 삭제할까요?')) return;
    await deleteBaseImage(baseImageId);
    const next = baseImages.filter((b) => b.base_image_id !== baseImageId);
    setBaseImages(next);
    if (selectedBaseImageId === baseImageId) {
      setSelectedBaseImageId(next[0]?.base_image_id ?? '');
    }
  };

  const handleDeleteClosetItem = async (itemId: string) => {
    if (!confirm('이 옷 아이템을 삭제할까요?')) return;
    await deleteClosetItem(itemId);
    setClosetItems((prev) => prev.filter((it) => it.itemId !== itemId));
    setSelectedClothesByCategory((prev) => {
      const next: Partial<Record<ClothesCategoryBackend, SelectedCloth>> = { ...prev };
      (Object.keys(next) as ClothesCategoryBackend[]).forEach((cat) => {
        if (next[cat]?.itemId === itemId) delete next[cat];
      });
      return next;
    });
  };

  const handleTryon = async () => {
    if (remainingCredits === 0) {
      setIsCreditExhaustedModalOpen(true);
      return;
    }

    if (!selectedBaseImageId.trim()) {
      alert('베이스 이미지를 선택해주세요.');
      return;
    }

    const parsedItemIds = (['TOP', 'BOTTOM', 'ACCESSORY'] as const)
      .map((cat) => selectedClothesByCategory[cat]?.itemId)
      .filter((v): v is string => Boolean(v));

    if (parsedItemIds.length === 0) {
      alert('옷을 최소 1개 선택해주세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      const res = await generateTryon({
        base_image_id: selectedBaseImageId.trim(),
        item_ids: parsedItemIds,
      });

      router.push(`/tryon/processing?job_id=${encodeURIComponent(res.job_id)}`);
    } catch (error) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const code = (error as any)?.code;
      if (code === 'ERR-103-A') {
        setIsCreditExhaustedModalOpen(true);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.studioPage}>
      {/* Main Preview Area (Background Layer) */}
      <div className={styles.previewContainer}>
        <div className={styles.mainImageWrapper}>
          {selectedPersonImageUrl ? (
            <img src={selectedPersonImageUrl} alt="Selected Base Model" className={styles.mainImage} />
          ) : (
            <div className={styles.mainImage} />
          )}
          
          {/* Floating Selected Clothes Badges */}
          {(['TOP', 'BOTTOM', 'ACCESSORY'] as const).map((cat, index) => {
            const cloth = selectedClothesByCategory[cat];
            if (!cloth) return null;
            const positionStyle =
              index === 0 ? { top: '30%', left: '20%' } :
              index === 1 ? { top: '50%', right: '20%' } :
              { bottom: '20%', left: '30%' };

            return (
              <div key={cat} className={styles.floatingBadge} style={positionStyle}>
                <button 
                  className={styles.removeBadgeBtn}
                  onClick={() => setSelectedClothesByCategory((prev) => {
                    const { [cat]: _, ...rest } = prev;
                    return rest;
                  })}
                >
                  <span className={styles.removeIcon}>✕</span>
                </button>
                <div className={styles.floatingContent}>
                  <img src={cloth.imageUrl} alt="Selected cloth" className={styles.floatingImage} />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Header */}
      <header className={styles.header}>
        <Link href="/tryon" className={styles.iconBtn}>
          <img src="/icons/arrow-left.png" alt="Back" className={styles.icon} />
        </Link>
      </header>

      {/* Bottom Overlay (Gradient & Controls) */}
      <div className={styles.bottomOverlay}>
        <input
          ref={baseImageInputRef}
          type="file"
          accept="image/*"
          style={{ display: 'none' }}
          onChange={(e) => handleBaseImagePicked(e.target.files?.[0] ?? null)}
        />
        <input
          ref={clothesImageInputRef}
          type="file"
          accept="image/*"
          style={{ display: 'none' }}
          onChange={async (e) => {
            const file = e.target.files?.[0] ?? null;
            if (!file) return;

            setIsUploadingClothes(true);
            try {
              const form = new FormData();
              form.append('imageFile', file);
              form.append('category', toBackendCategory(clothesCategory));
              await addClosetItem(form);
              await refreshClosetItems();
            } finally {
              setIsUploadingClothes(false);
              if (clothesImageInputRef.current) clothesImageInputRef.current.value = '';
            }
          }}
        />
        {/* Controls Row */}
      <div className={styles.controlsRow}>
        <Toggle 
          options={[
            { id: 'person', label: '인물' },
            { id: 'clothes', label: '옷' }
          ]}
          activeId={activeTab}
          onChange={(id) => setActiveTab(id as 'person' | 'clothes')}
        />

        {activeTab === 'clothes' && (
          <Toggle 
            options={[
              { id: 'top', label: '상의' },
              { id: 'bottom', label: '하의' },
              { id: 'accessory', label: '악세서리' }
            ]}
            activeId={clothesCategory}
            onChange={(id) => setClothesCategory(id as ClothesCategoryUi)}
          />
        )}
      </div>

      {/* Item Carousel */}
        <div className={styles.carouselContainer}>
          <div className={styles.carousel}>
            {/* Add / Upload Card */}
            <div className={styles.itemCard} onClick={handleUploadClick}>
              <div className={styles.uploadInner}>
                <span className={styles.plusIcon}>
                  {activeTab === 'person' && isUploadingBaseImage
                    ? '…'
                    : activeTab === 'clothes' && isUploadingClothes
                      ? '…'
                      : '+'}
                </span>
              </div>
            </div>

            {/* Person List */}
            {activeTab === 'person' && (
              <>
                {baseImages.length === 0 && (
                  <div style={{ padding: '12px 4px', fontSize: 12, opacity: 0.7 }}>
                    베이스 이미지가 없습니다. + 버튼으로 업로드해주세요.
                  </div>
                )}
                {baseImages.map((img, i) => (
                  <div
                    key={img.base_image_id}
                    className={`${styles.itemCard} ${selectedBaseImageId === img.base_image_id ? styles.selectedCard : ''}`}
                    onClick={() => setSelectedBaseImageId(img.base_image_id)}
                  >
                    <img src={img.image_url} alt={`Person ${i}`} className={styles.itemImage} />
                    <button
                      type="button"
                      className={styles.deleteBtn}
                      onClick={(e) => {
                        e.stopPropagation();
                        void handleDeleteBaseImage(img.base_image_id);
                      }}
                      aria-label="베이스 이미지 삭제"
                      title="삭제"
                    >
                      ✕
                    </button>
                    {selectedBaseImageId === img.base_image_id && <div className={styles.checkBadge}>✓</div>}
                  </div>
                ))}
              </>
            )}

          {/* Clothes List */}
          {activeTab === 'clothes' && (
            <>
              {closetItems.length === 0 && (
                <div style={{ padding: '12px 4px', fontSize: 12, opacity: 0.7 }}>
                  {clothesCategoryLabel(clothesCategory)}가 선택되지 않았습니다.
                </div>
              )}
              {closetItems.map((cloth) => {
                const currentCategory = toBackendCategory(clothesCategory);
                const isSelected = selectedClothesByCategory[currentCategory]?.itemId === cloth.itemId;
                return (
                  <div
                    key={cloth.itemId}
                    className={`${styles.itemCard} ${isSelected ? styles.selectedCard : ''}`}
                    onClick={() => handleToggleCloth(cloth)}
                  >
                    <div className={styles.clothContent}>
                      <img src={cloth.imageUrl} alt={`Cloth ${cloth.itemId}`} className={styles.itemImage} />
                    </div>
                    <button
                      type="button"
                      className={styles.deleteBtn}
                      onClick={(e) => {
                        e.stopPropagation();
                        void handleDeleteClosetItem(cloth.itemId);
                      }}
                      aria-label="옷 아이템 삭제"
                      title="삭제"
                    >
                      ✕
                    </button>
                    {isSelected && <div className={styles.checkBadge}>✓</div>}
                  </div>
                );
              })}
            </>
          )}
        </div>
      </div>

        {/* Bottom Action */}
        <div className={styles.bottomAction}>
          <Button variant="large" fullWidth onClick={handleTryon} disabled={isSubmitting}>
            {isSubmitting ? '시착 생성 중...' : '시착하기'}
          </Button>
        </div>
      </div>

      <ConfirmModal
        isOpen={isCreditExhaustedModalOpen}
        title="오늘의 무료 시착 크레딧을 모두 사용했어요"
        description="크레딧은 매일 24시(KST)에 자동으로 초기화됩니다."
        confirmText="확인"
        cancelText=""
        onConfirm={() => setIsCreditExhaustedModalOpen(false)}
        onCancel={() => setIsCreditExhaustedModalOpen(false)}
      />
    </div>
  );
}
