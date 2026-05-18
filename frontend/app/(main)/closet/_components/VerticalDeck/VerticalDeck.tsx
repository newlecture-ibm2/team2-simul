'use client';

import { useRef, useState, useLayoutEffect } from 'react';
import ClosetCard from '../ClosetCard/ClosetCard';
import styles from './VerticalDeck.module.css';

interface Item {
  id: string;
  imageUrl?: string;
}

interface VerticalDeckProps {
  items: Item[];
  onItemClick: (id: string) => void;
}

export default function VerticalDeck({ items, onItemClick }: VerticalDeckProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const viewportRef = useRef<HTMLDivElement>(null);
  const scrubberTrackRef = useRef<HTMLDivElement>(null);
  const [scrollY, setScrollY] = useState(0);
  const [viewportHeight, setViewportHeight] = useState(600);

  // Create a massive virtual scroll space
  const ITEM_COUNT = items?.length || 0;
  const VIRTUAL_MULTIPLIER = 1000; 
  const totalVirtualItems = ITEM_COUNT * VIRTUAL_MULTIPLIER;
  
  // Tuning parameters — scale with viewport height
  const scrollSensitivity = 150; // How many px of scroll = 1 card movement

  useLayoutEffect(() => {
    if (containerRef.current && ITEM_COUNT > 0) {
      setViewportHeight(containerRef.current.clientHeight);
      // Start near the middle
      const startScroll = (totalVirtualItems / 2) * scrollSensitivity;
      containerRef.current.scrollTop = startScroll;
      setScrollY(startScroll);
    }
  }, [totalVirtualItems, scrollSensitivity, ITEM_COUNT]);

  if (!items || items.length === 0) {
    return <div className={styles.emptyDeck}>아이템이 없습니다.</div>;
  }

  const handleScroll = () => {
    if (containerRef.current) {
      setScrollY(containerRef.current.scrollTop);
    }
  };

  // Dynamic: scale K_FACTOR with viewport height (taller = more spread)
  // Base: 0.18 at 400px, scales up linearly
  const K_FACTOR = Math.max(0.12, 0.18 * (viewportHeight / 400));

  const exactCenterIndex = scrollY / scrollSensitivity;
  const roundedCenterIndex = Math.floor(exactCenterIndex);
  
  // Scrubber Event Handlers
  const handleScrubberMove = (clientY: number) => {
    if (!scrubberTrackRef.current || !containerRef.current) return;
    const rect = scrubberTrackRef.current.getBoundingClientRect();
    let percent = (clientY - rect.top) / rect.height;
    percent = Math.max(0, Math.min(1, percent));
    
    // Convert percent back to a real index (0 to ITEM_COUNT - 1)
    const targetRealIndex = percent * (ITEM_COUNT - 1);
    
    // Find the current cycle we are in so the deck doesn't jump wildly
    const currentCycle = Math.floor(exactCenterIndex / ITEM_COUNT);
    const targetVirtualIndex = currentCycle * ITEM_COUNT + targetRealIndex;
    
    // Update scroll
    containerRef.current.scrollTop = targetVirtualIndex * scrollSensitivity;
  };

  const onScrubberPointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.currentTarget.setPointerCapture(e.pointerId);
    handleScrubberMove(e.clientY);
  };

  const onScrubberPointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (e.buttons > 0) { // If holding button/touch
      handleScrubberMove(e.clientY);
    }
  };

  // Dynamic: more cards rendered on taller screens
  // Base: 8 at 400px, scales up (e.g. 12 at 600px, 16 at 800px)
  const renderRange = Math.max(6, Math.round(8 * (viewportHeight / 400)));
  const startIndex = roundedCenterIndex - renderRange;
  const endIndex = roundedCenterIndex + renderRange;

  const MAX_Y = viewportHeight / 2 - 40; // Dynamically follows viewport height

  const visibleItems = [];
  for (let i = startIndex; i <= endIndex; i++) {
    // Map virtual index to real data index
    const realItem = items[((i % ITEM_COUNT) + ITEM_COUNT) % ITEM_COUNT];
    
    // Distance from the absolute center of the viewport
    const distanceInIndex = i - exactCenterIndex;
    
    const sign = Math.sign(distanceInIndex);
    const absDist = Math.abs(distanceInIndex);
    
    // Non-linear Y position mapping: closer cards are spaced out, further cards stack up tightly
    const yOffset = sign * MAX_Y * (1 - Math.exp(-K_FACTOR * absDist));
    
    // Scale: very subtle reduction at the edges
    const scale = 1 - (absDist * 0.04); 
    const clampedScale = Math.max(0.6, scale);
    
    // Opacity: fade out only the deepest cards
    const opacity = 1 - (absDist * 0.1);
    const clampedOpacity = Math.max(0, opacity);
    
    // Z-index calculation: center is highest
    const zIndex = 1000 - Math.floor(absDist * 10);
    
    // Rotate to enhance 3D overlap effect (top tilts back, bottom tilts forward)
    const rotateX = distanceInIndex * -5;

    visibleItems.push({
      key: `virtual-${i}`,
      item: realItem,
      style: {
        transform: `translate(-50%, -50%) perspective(1200px) translateY(${yOffset}px) rotateX(${rotateX}deg) scale(${clampedScale})`,
        opacity: clampedOpacity,
        zIndex,
      }
    });
  }

  // Reverse the array so the DOM drawing order natively helps with some rendering, 
  // though z-index handles the strict stacking.
  visibleItems.reverse();

  // Scrubber thumb position and date
  const currentRealIndex = ((roundedCenterIndex % ITEM_COUNT) + ITEM_COUNT) % ITEM_COUNT;
  const scrubberPercent = currentRealIndex / Math.max(1, ITEM_COUNT - 1);
  
  // Generate a fake date based on the real index for demo purposes
  const demoDate = new Date();
  demoDate.setDate(demoDate.getDate() - currentRealIndex * 5); // 5 days apart
  const formattedDate = demoDate.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });

  return (
    <div 
      className={styles.deckContainer} 
      ref={containerRef} 
      onScroll={handleScroll}
    >
      <div 
        className={styles.scrollSpacer}
        style={{ height: `${totalVirtualItems * scrollSensitivity}px` }}
      >
        <div className={styles.viewport} ref={viewportRef} style={{ height: viewportHeight || 'calc(100dvh - 150px)' }}>
          {/* Timeline Scrubber */}
          <div className={styles.scrubberContainer}>
            <div 
              className={styles.scrubberTrack} 
              ref={scrubberTrackRef}
              onPointerDown={onScrubberPointerDown}
              onPointerMove={onScrubberPointerMove}
            >
              <div 
                className={styles.scrubberThumb} 
                style={{ top: `${scrubberPercent * 100}%` }}
              >
                <div className={styles.scrubberLabel}>{formattedDate}</div>
              </div>
            </div>
          </div>

          {visibleItems.map(({ key, item, style }) => (
            <div key={key} className={styles.cardWrapper} style={style}>
              <ClosetCard 
                id={item.id} 
                imageUrl={item.imageUrl} 
                onClick={onItemClick} 
              />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
