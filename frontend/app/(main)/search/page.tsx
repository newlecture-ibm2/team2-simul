'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './page.module.css';
import SearchBar from './_components/SearchBar';
import SearchFilter from './_components/SearchFilter';
import SearchResultGrid from './_components/SearchResultGrid';
import { SearchType } from '../../../lib/api/searchAPI';

export default function SearchPage() {
  const router = useRouter();
  const [query, setQuery] = useState('');
  const [searchType, setSearchType] = useState<SearchType>('all');

  const handleSearch = (newQuery: string) => {
    setQuery(newQuery);
  };

  const handleBack = () => {
    router.back();
  };

  return (
    <div className={styles.pageContainer}>
      <header className={styles.header}>
        <button className={styles.backButton} onClick={handleBack} aria-label="뒤로가기">
          ←
        </button>
        <SearchBar onSearch={handleSearch} />
      </header>

      {query ? (
        <div className={styles.content}>
          <SearchFilter currentType={searchType} onChange={setSearchType} />
          <SearchResultGrid query={query} type={searchType} />
        </div>
      ) : (
        <div className={styles.initialState}>
          <div className={styles.initialIcon}>🔍</div>
          <h2 className={styles.initialText}>무엇을 찾고 싶으신가요?</h2>
          <p className={styles.initialSubtext}>태그나 본문 내용으로 게시물을 검색해보세요.</p>
        </div>
      )}
    </div>
  );
}
