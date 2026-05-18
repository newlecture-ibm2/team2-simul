'use client';

import { useState } from 'react';
import styles from './page.module.css';
import Sidebar from './_components/Sidebar';
import ReportManagementSection from './_components/ReportManagementSection';

import UserManagementSection from './_components/UserManagementSection';

type Tab = 'reports' | 'users';

export default function AdminPage() {
  const [activeTab, setActiveTab] = useState<Tab>('reports');

  return (
    <div className={styles.container}>
      <Sidebar activeTab={activeTab} onChangeTab={setActiveTab} />
      <div className={styles.content}>
        {activeTab === 'reports' && <ReportManagementSection />}
        {activeTab === 'users' && <UserManagementSection />}
      </div>
    </div>
  );
}
