'use client';

import { usePathname } from 'next/navigation';
import { ReactNode } from 'react';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import BottomNav from '@/components/BottomNav';
import styles from '@/app/layout.module.css';

interface AppLayoutWrapperProps {
  children: ReactNode;
  appClassName: string;
  adminClassName: string;
}

export default function AppLayoutWrapper({ children, appClassName, adminClassName }: AppLayoutWrapperProps) {
  const pathname = usePathname();
  const isAdmin = pathname?.startsWith('/admin');
  const isSplash = pathname === '/splash';

  return (
    <div className={isAdmin ? adminClassName : appClassName}>
      {!isAdmin && !isSplash && <Header />}
      <main className={styles.main}>{children}</main>
      {!isAdmin && !isSplash && <Footer />}
      {!isAdmin && !isSplash && <BottomNav />}
    </div>
  );
}
