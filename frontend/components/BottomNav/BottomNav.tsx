'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import styles from './BottomNav.module.css';

const TABS = [
  { href: '/', iconName: 'home' },
  { href: '/tryon', iconName: 'tryon' },
  { href: '/post/create', iconName: 'create' },
  { href: '/closet', iconName: 'closet' },
  { href: '/profile', iconName: 'profile' },
];

export default function BottomNav() {
  const pathname = usePathname();

  const isActive = (href: string) => {
    if (href === '/') return pathname === '/';
    return pathname.startsWith(href);
  };

  return (
    <nav className={styles.bottomNav} aria-label="하단 네비게이션">
      {TABS.map((tab) => {
        const active = isActive(tab.href);
        const iconSrc = `/icons/${tab.iconName}.png`;
        
        return (
          <Link
            key={tab.href}
            href={tab.href}
            className={`${styles.navItem} ${active ? styles.navItemActive : ''}`}
          >
            <img src={iconSrc} alt={`${tab.iconName} 탭`} className={styles.navIcon} />
          </Link>
        );
      })}
    </nav>
  );
}
