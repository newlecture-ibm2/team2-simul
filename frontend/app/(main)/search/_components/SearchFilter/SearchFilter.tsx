'use client';

import styles from './SearchFilter.module.css';
import { SearchType } from '../../../../../lib/api/searchAPI';

interface SearchFilterProps {
  currentType: SearchType;
  onChange: (type: SearchType) => void;
}

export default function SearchFilter({ currentType, onChange }: SearchFilterProps) {
  return (
    <div className={styles.filterContainer}>
      <button
        className={`${styles.filterButton} ${currentType === 'all' ? styles.active : ''}`}
        onClick={() => onChange('all')}
      >
        전체
      </button>
      <button
        className={`${styles.filterButton} ${currentType === 'tag' ? styles.active : ''}`}
        onClick={() => onChange('tag')}
      >
        태그
      </button>
      <button
        className={`${styles.filterButton} ${currentType === 'caption' ? styles.active : ''}`}
        onClick={() => onChange('caption')}
      >
        본문
      </button>
    </div>
  );
}
