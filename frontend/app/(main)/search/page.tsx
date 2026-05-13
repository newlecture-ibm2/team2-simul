'use client';

import { useState, Suspense, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import styles from './page.module.css';
import SearchBar from './_components/SearchBar';
import SearchFilter from './_components/SearchFilter';
import SearchResultGrid from './_components/SearchResultGrid';
import { SearchType } from '../../../lib/api/searchAPI';

function SearchContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  
  const initialQuery = searchParams.get('q') || '';
  const initialType = (searchParams.get('type') as SearchType) || 'all';

  const [query, setQuery] = useState(initialQuery);
  const [searchType, setSearchType] = useState<SearchType>(initialType);

  useEffect(() => {
    const q = searchParams.get('q') || '';
    const type = (searchParams.get('type') as SearchType) || 'all';
    setQuery(q);
    setSearchType(type);
  }, [searchParams]);

  const updateUrl = (q: string, type: SearchType) => {
    if (!q) {
      router.replace('/search');
    } else {
      router.replace(`/search?q=${encodeURIComponent(q)}&type=${type}`);
    }
  };

  const handleSearch = (newQuery: string) => {
    setQuery(newQuery);
    updateUrl(newQuery, searchType);
  };

  const handleTypeChange = (newType: SearchType) => {
    setSearchType(newType);
    updateUrl(query, newType);
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
        <SearchBar initialQuery={query} onSearch={handleSearch} />
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

export default function SearchPage() {
  return (
    <Suspense fallback={<div className={styles.pageContainer}></div>}>
      <SearchContent />
    </Suspense>
  );
}
