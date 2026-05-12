'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Button from '@/components/Button';
import Toggle from './_components/Toggle/Toggle';
import styles from './page.module.css';
import { generateTryon } from '@/lib/api/tryonAPI';

const DUMMY_PEOPLE = ['/dummy.jpg', '/recent.jpg', '/temp.jpg'];
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
  const [selectedPerson, setSelectedPerson] = useState<string>(DUMMY_PEOPLE[0]);
  const [selectedClothes, setSelectedClothes] = useState<number[]>([]); // Store selected clothes IDs
  const [baseImageId, setBaseImageId] = useState<string>('');
  const [itemIds, setItemIds] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleToggleCloth = (id: number) => {
    setSelectedClothes(prev => 
      prev.includes(id) ? prev.filter(cId => cId !== id) : [...prev, id]
    );
  };

  const handleUploadClick = () => {
    // 임시: 업로드 클릭 시 아무 동작 안 함 (추후 모달 띄우기)
  };

  const handleTryon = async () => {
    if (!baseImageId.trim()) {
      alert('개발자 입력(base_image_id)이 필요합니다.');
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
        base_image_id: baseImageId.trim(),
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
          <img src={selectedPerson} alt="Selected Base Model" className={styles.mainImage} />
          
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
        <details style={{ width: '100%', marginBottom: 12 }}>
          <summary style={{ cursor: 'pointer' }}>개발자 입력 (임시)</summary>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 8 }}>
            <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <span style={{ fontSize: 12, opacity: 0.7 }}>base_image_id (UUID)</span>
              <input
                value={baseImageId}
                onChange={(e) => setBaseImageId(e.target.value)}
                placeholder="예: 00000000-0000-0000-0000-000000000000"
                style={{ padding: 10, borderRadius: 8, border: '1px solid rgba(0,0,0,0.15)' }}
              />
            </label>
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
              <span className={styles.plusIcon}>+</span>
            </div>
          </div>

          {/* Person List */}
          {activeTab === 'person' && DUMMY_PEOPLE.map((imgSrc, i) => (
            <div 
              key={i} 
              className={`${styles.itemCard} ${selectedPerson === imgSrc ? styles.selectedCard : ''}`}
              onClick={() => setSelectedPerson(imgSrc)}
            >
              <img src={imgSrc} alt={`Person ${i}`} className={styles.itemImage} />
              {selectedPerson === imgSrc && (
                <div className={styles.checkBadge}>✓</div>
              )}
            </div>
          ))}

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
