import type { Metadata, Viewport } from 'next';
import './globals.css';
import styles from './layout.module.css';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import BottomNav from '@/components/BottomNav';
import Providers from '@/components/Providers';

import OfflineBanner from '@/components/OfflineBanner';
import Toast from '@/components/Toast';
import GlobalLoading from '@/components/GlobalLoading';
import AuthInitializer from '@/components/AuthInitializer';
import { NotificationInitializer } from '@/components/NotificationInitializer';

export const metadata: Metadata = {
  title: 'SIMUL — AI 가상시착 패션 플랫폼',
  description:
    'AI 가상시착으로 나만의 스타일을 발견하세요. 패션을 탐색하고, 시착하고, 공유하는 새로운 경험.',
};

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  viewportFit: 'cover',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>
        <Providers>
          <OfflineBanner />
          <Toast />
          <GlobalLoading />
          <AuthInitializer />
          <NotificationInitializer />
          <div className={styles.appContainer}>
            <Header />
            <main className={styles.main}>{children}</main>
            <Footer />
            <BottomNav />
          </div>
        </Providers>
      </body>
    </html>
  );
}
