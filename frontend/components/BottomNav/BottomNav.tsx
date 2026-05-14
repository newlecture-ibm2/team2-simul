'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import styles from './BottomNav.module.css';

const TABS = [
  { href: '/', iconName: 'home', authRequired: false },
  { href: '/community', iconName: 'rectangle.portrait.on.rectangle.portrait.angled', authRequired: false },
  { href: '/post/create', iconName: 'create', authRequired: true },
  { href: '/tryon', iconName: 'tryon', authRequired: true },
  { href: '/closet', iconName: 'closet', authRequired: true },
];

interface CustomWindow extends Window {
  isNavigatingFromBottomNav?: boolean;
}

export default function BottomNav() {
  const pathname = usePathname();
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();

  const isActive = (href: string) => {
    if (href === '/') return pathname === '/';
    return pathname.startsWith(href);
  };

  const handleNavClick = (e: React.MouseEvent<HTMLAnchorElement>, tab: typeof TABS[0]) => {
    if (typeof window !== 'undefined') {
      (window as CustomWindow).isNavigatingFromBottomNav = true;
    }

    if (tab.authRequired && !isAuthenticated) {
      e.preventDefault();
      router.push(`/login?returnUrl=${tab.href}`);
    }
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
            onClick={(e) => handleNavClick(e, tab)}
          >
            <img src={iconSrc} alt={`${tab.iconName} 탭`} className={styles.navIcon} />
          </Link>
        );
      })}
    </nav>
  );
}
