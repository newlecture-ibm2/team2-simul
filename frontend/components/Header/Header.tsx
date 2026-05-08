'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import styles from './Header.module.css';

export default function Header() {
  return (
    <header className={styles.header}>
      <div className={styles.headerInner}>
        <Link href="/" className={styles.logo}>
          <img src="/logo.png" alt="SIMUL" className={styles.logoImage} />
        </Link>

        <div className={styles.rightIcons}>
          <button className={styles.iconBtn} aria-label="검색">
            <img src="/icons/magnifyingglass.png" alt="검색 아이콘" className={styles.iconImage} />
          </button>
          <button className={styles.iconBtn} aria-label="알림">
            <img src="/icons/ring.png" alt="알림" className={styles.iconImage} />
          </button>
        </div>
      </div>
    </header>
  );
}
