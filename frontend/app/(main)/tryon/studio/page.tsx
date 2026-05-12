'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Button from '@/components/Button';
import Toggle from './_components/Toggle/Toggle';
import styles from './page.module.css';
import { generateTryon, getMyBaseImages, uploadBaseImage } from '@/lib/api/tryonAPI';

const DUMMY_CLOTHES = [
  { id: 1, image: '/clothes.png' },
  { id: 2, image: '/clothes.png' },
  { id: 3, image: '/clothes.png' },
  { id: 4, image: '/clothes.png' },
  { id: 5, image: '/clothes.png' },
];

export default function StudioPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<'person' | 'clothes'>('clothes');
  const [clothesCategory, setClothesCategory] = useState<'top' | 'bottom'>('top');
  const [baseImages, setBaseImages] = useState<Array<{ base_image_id: string; image_url: string }>>([]);
  const [selectedBaseImageId, setSelectedBaseImageId] = useState<string>('');
  const [selectedClothes, setSelectedClothes] = useState<number[]>([]); // Store selected clothes IDs
  const [itemIds, setItemIds] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isUploadingBaseImage, setIsUploadingBaseImage] = useState(false);
  const baseImageInputRef = useRef<HTMLInputElement | null>(null);

  const selectedPersonImageUrl = useMemo(() => {
    const picked = baseImages.find(b => b.base_image_id === selectedBaseImageId);
    return picked?.image_url || '/dummy.jpg';
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

  const handleToggleCloth = (id: number) => {
    setSelectedClothes(prev => 
      prev.includes(id) ? prev.filter(cId => cId !== id) : [...prev, id]
    );
  };

  const handleUploadClick = () => {
    if (activeTab === 'person') {
      baseImageInputRef.current?.click();
      return;
    }

    // 옷 업로드는 추후 구현
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

  const handleTryon = async () => {
    if (!selectedBaseImageId.trim()) {
      alert('베이스 이미지를 선택해주세요.');
      return;
    }

    const parsedItemIds = itemIds
      .split(',')
      .map(v => v.trim())
      .filter(Boolean);

    if (parsedItemIds.length === 0) {
      alert('개발자 입력(item_ids)이 최소 1개 필요합니다.');
      return;
    }

    setIsSubmitting(true);
    try {
      const res = await generateTryon({
        base_image_id: selectedBaseImageId.trim(),
        item_ids: parsedItemIds,
      });

      router.push(`/tryon/processing?job_id=${encodeURIComponent(res.job_id)}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.studioPage}>
      {/* Main Preview Area (Background Layer) */}
      <div className={styles.previewContainer}>
        <div className={styles.mainImageWrapper}>
          <img src={selectedPersonImageUrl} alt="Selected Base Model" className={styles.mainImage} />
          
          {/* Floating Selected Clothes Badges */}
          {selectedClothes.map((id, index) => {
            const cloth = DUMMY_CLOTHES.find(c => c.id === id);
            if (!cloth) return null;
            // 각 옷 타입별로 임의의 위치 지정 (레퍼런스 참고)
            const positionStyle = 
              index === 0 ? { top: '30%', left: '20%' } : 
              index === 1 ? { top: '50%', right: '20%' } : 
              { bottom: '20%', left: '30%' };

            return (
              <div key={id} className={styles.floatingBadge} style={positionStyle}>
                <button 
                  className={styles.removeBadgeBtn}
                  onClick={() => handleToggleCloth(id)}
                >
                  <span className={styles.removeIcon}>✕</span>
                </button>
                <div className={styles.floatingContent}>
                  <img src={cloth.image} alt="Selected cloth" className={styles.floatingImage} />
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

        <details style={{ width: '100%', marginBottom: 12 }}>
          <summary style={{ cursor: 'pointer' }}>개발자 입력 (임시)</summary>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 8 }}>
            <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <span style={{ fontSize: 12, opacity: 0.7 }}>item_ids (UUID, 콤마 구분)</span>
              <input
                value={itemIds}
                onChange={(e) => setItemIds(e.target.value)}
                placeholder="예: uuid1, uuid2, uuid3"
                style={{ padding: 10, borderRadius: 8, border: '1px solid rgba(0,0,0,0.15)' }}
              />
            </label>
          </div>
        </details>
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
              { id: 'bottom', label: '하의' }
            ]}
            activeId={clothesCategory}
            onChange={(id) => setClothesCategory(id as 'top' | 'bottom')}
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
                  {activeTab === 'person' && isUploadingBaseImage ? '…' : '+'}
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
                    {selectedBaseImageId === img.base_image_id && <div className={styles.checkBadge}>✓</div>}
                  </div>
                ))}
              </>
            )}

          {/* Clothes List */}
          {activeTab === 'clothes' && DUMMY_CLOTHES.map((cloth) => {
            const isSelected = selectedClothes.includes(cloth.id);
            return (
              <div 
                key={cloth.id} 
                className={`${styles.itemCard} ${isSelected ? styles.selectedCard : ''}`}
                onClick={() => handleToggleCloth(cloth.id)}
              >
                <div className={styles.clothContent}>
                  <img src={cloth.image} alt={`Cloth ${cloth.id}`} className={styles.itemImage} />
                </div>
                {isSelected && (
                  <div className={styles.checkBadge}>✓</div>
                )}
              </div>
            );
          })}
        </div>
      </div>

        {/* Bottom Action */}
        <div className={styles.bottomAction}>
          <Button variant="large" fullWidth onClick={handleTryon} disabled={isSubmitting}>
            {isSubmitting ? '시착 생성 중...' : '시착하기'}
          </Button>
        </div>
      </div>
    </div>
  );
}
