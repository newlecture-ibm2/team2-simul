'use client';

import Link from 'next/link';
import styles from './Sidebar.module.css';

type Tab = 'reports' | 'users';

interface SidebarProps {
  activeTab: Tab;
  onChangeTab: (tab: Tab) => void;
}

export default function Sidebar({ activeTab, onChangeTab }: SidebarProps) {
  return (
    <aside className={styles.sidebar}>
      <h2 className={styles.title}>관리자 대시보드</h2>
      <nav className={styles.nav}>
        <button
          className={`${styles.navItem} ${activeTab === 'reports' ? styles.active : ''}`}
          onClick={() => onChangeTab('reports')}
        >
          신고 관리
        </button>
        <button
          className={`${styles.navItem} ${activeTab === 'users' ? styles.active : ''}`}
          onClick={() => onChangeTab('users')}
        >
          유저 관리
        </button>
      </nav>
    </aside>
  );
}
