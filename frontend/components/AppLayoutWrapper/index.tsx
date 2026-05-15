'use client';

import { usePathname } from 'next/navigation';
import { ReactNode } from 'react';

interface AppLayoutWrapperProps {
  children: ReactNode;
  appClassName: string;
  adminClassName: string;
}

export default function AppLayoutWrapper({ children, appClassName, adminClassName }: AppLayoutWrapperProps) {
  const pathname = usePathname();
  const isAdmin = pathname?.startsWith('/admin');

  return (
    <div className={isAdmin ? adminClassName : appClassName}>
      {children}
    </div>
  );
}
