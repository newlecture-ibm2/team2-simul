'use client';

import { usePathname } from 'next/navigation';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import BottomNav from '@/components/BottomNav';
import styles from '@/app/layout.module.css';

export default function ConditionalLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isSplash = pathname === '/splash';

  return (
    <div className={styles.appContainer}>
      {!isSplash && <Header />}
      <main className={styles.main}>{children}</main>
      {!isSplash && <Footer />}
      {!isSplash && <BottomNav />}
    </div>
  );
}
