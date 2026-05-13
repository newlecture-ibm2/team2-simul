'use client';

import { useState, useEffect, useRef } from 'react';
import styles from './SearchBar.module.css';
import { searchTags, TagResponse } from '../../../../../lib/api/tagAPI';

interface SearchBarProps {
  initialQuery?: string;
  onSearch: (query: string) => void;
}

export default function SearchBar({ initialQuery = '', onSearch }: SearchBarProps) {
  const [query, setQuery] = useState(initialQuery);
  const [suggestions, setSuggestions] = useState<TagResponse[]>([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const debounceTimer = useRef<NodeJS.Timeout | null>(null);

  // 외부 영역 클릭 시 드롭다운 닫기
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.addEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // 디바운스를 적용한 자동완성 API 호출
  useEffect(() => {
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    if (!query.trim()) {
      setSuggestions([]);
      setIsDropdownOpen(false);
      return;
    }

    // 만약 사용자가 #으로 시작하면 #을 제외하고 검색
    const searchQuery = query.startsWith('#') ? query.slice(1) : query;

    if (!searchQuery.trim()) {
      setSuggestions([]);
      return;
    }

    debounceTimer.current = setTimeout(async () => {
      try {
        const results = await searchTags(searchQuery);
        setSuggestions(results);
        setIsDropdownOpen(results.length > 0);
      } catch (error) {
        console.error('Failed to fetch tag suggestions', error);
      }
    }, 300);

    return () => {
      if (debounceTimer.current) clearTimeout(debounceTimer.current);
    };
  }, [query]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) {
      setIsDropdownOpen(false);
      onSearch(query.trim());
    }
  };

  const handleClear = () => {
    setQuery('');
    setSuggestions([]);
    setIsDropdownOpen(false);
    onSearch('');
  };

  const handleSuggestionClick = (tagName: string) => {
    const newQuery = `#${tagName}`;
    setQuery(newQuery);
    setIsDropdownOpen(false);
    onSearch(newQuery);
  };

  return (
    <div className={styles.searchContainer} ref={dropdownRef}>
      <span className={styles.searchIcon}>🔍</span>
      <form onSubmit={handleSubmit} style={{ flex: 1, display: 'flex' }}>
        <input
          type="text"
          className={styles.input}
          placeholder="Search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => {
            if (suggestions.length > 0) setIsDropdownOpen(true);
          }}
        />
        {query && (
          <button type="button" className={styles.clearButton} onClick={handleClear}>
            ✕
          </button>
        )}
      </form>

      {isDropdownOpen && suggestions.length > 0 && (
        <div className={styles.dropdown}>
          {suggestions.map((tag) => (
            <div 
              key={tag.tagId} 
              className={styles.dropdownItem}
              onClick={() => handleSuggestionClick(tag.name)}
            >
              <div className={styles.tagContent}>
                <span className={styles.hashIcon}>#</span>
                <span className={styles.tagName}>{tag.name}</span>
              </div>
              <span className={styles.usageCount}>게시물 {tag.usageCount.toLocaleString()}개</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
