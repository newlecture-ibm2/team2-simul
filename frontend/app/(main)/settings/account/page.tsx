'use client';

import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import WithdrawButton from './_components/WithdrawButton';
import styles from './page.module.css';

export default function AccountSettingsPage() {
  const router = useRouter();
  const { user } = useAuthStore();

  return (
    <div className={styles.accountPage}>
      {/* Header */}
      <header className={styles.header}>
        <button onClick={() => router.back()} className={styles.backBtn} aria-label="뒤로가기">
          ←
        </button>
        <h1 className={styles.headerTitle}>계정 정보</h1>
      </header>

      {/* Profile Overview Card */}
      <div className={styles.profileCard}>
        <img 
          src={user?.profileImageUrl || '/profile.jpg'} 
          alt="Profile Avatar" 
          className={styles.avatar} 
        />
        <div className={styles.profileInfo}>
          <span className={styles.nickname}>{user?.nickname || '사용자'}</span>
          {user?.email && (
            <span className={styles.email}>{user.email}</span>
          )}
        </div>
      </div>

      {/* Account Provider Details */}
      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>계정 연결 정보</h2>
        
        <div className={styles.settingItem}>
          <div className={styles.settingLabel}>
            <span className={styles.settingName}>로그인 수단</span>
          </div>
          <span className={`${styles.providerTag} ${user?.provider ? styles['provider' + (user.provider.charAt(0).toUpperCase() + user.provider.slice(1))] : ''}`}>
            {user?.provider === 'email' ? '이메일 로그인' : user?.provider}
          </span>
        </div>

        {user?.provider === 'email' && (
          <a href="/settings/password" className={styles.linkItem}>
            <span className={styles.settingName}>비밀번호 변경</span>
            <span className={styles.linkArrow}>→</span>
          </a>
        )}
      </div>

      {/* Account Deletion / Withdrawal */}
      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>계정 관리</h2>
        
        <div className={styles.withdrawRow}>
          <div className={styles.withdrawText}>
            <span className={styles.settingName}>회원 탈퇴</span>
            <span className={styles.settingDesc}>
              탈퇴 시 등록하신 가상 시착 이력과 옷장 데이터가 영구적으로 파기되며 복구할 수 없습니다.
            </span>
          </div>
          <WithdrawButton />
        </div>
      </div>
    </div>
  );
}
