'use client';

import styles from './page.module.css';
import LogoutButton from './_components/LogoutButton';
import { useAuthStore } from '@/lib/stores/useAuthStore';



export default function SettingsPage() {
  const { user } = useAuthStore();

  return (
    <div className={styles.settingsPage}>
      <h1>설정</h1>

      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>알림</h2>
        <div className={styles.settingItem}>
          <div className={styles.settingLabel}>
            <span className={styles.settingName}>좋아요 알림</span>
            <span className={styles.settingDesc}>게시물에 좋아요가 달리면 알림</span>
          </div>
          <button className={`${styles.toggle} ${styles.toggleActive}`} aria-label="좋아요 알림 토글" />
        </div>
        <div className={styles.settingItem}>
          <div className={styles.settingLabel}>
            <span className={styles.settingName}>댓글 알림</span>
            <span className={styles.settingDesc}>새 댓글이 달리면 알림</span>
          </div>
          <button className={`${styles.toggle} ${styles.toggleActive}`} aria-label="댓글 알림 토글" />
        </div>
        <div className={styles.settingItem}>
          <div className={styles.settingLabel}>
            <span className={styles.settingName}>팔로우 알림</span>
            <span className={styles.settingDesc}>새 팔로워가 생기면 알림</span>
          </div>
          <button className={styles.toggle} aria-label="팔로우 알림 토글" />
        </div>
        <div className={styles.settingItem}>
          <div className={styles.settingLabel}>
            <span className={styles.settingName}>시착 완료 알림</span>
            <span className={styles.settingDesc}>AI 시착이 완료되면 알림</span>
          </div>
          <button className={`${styles.toggle} ${styles.toggleActive}`} aria-label="시착 완료 알림 토글" />
        </div>
      </div>

      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>계정</h2>
        {user?.provider === 'email' ? (
          <>
            <a href="/settings/password" className={styles.linkItem}>
              <span className={styles.settingName}>비밀번호 변경</span>
              <span className={styles.linkArrow}>→</span>
            </a>
          </>
        ) : (
          <div className={styles.linkItem}>
            <span className={styles.settingName}>연결된 계정</span>
            <span className={`${styles.providerTag} ${user?.provider ? styles['provider' + (user.provider.charAt(0).toUpperCase() + user.provider.slice(1))] : ''}`}>
              {user?.provider}
            </span>
          </div>
        )}
      </div>

      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>정보</h2>
        <a href="#" className={styles.linkItem}>
          <span className={styles.settingName}>이용약관</span>
          <span className={styles.linkArrow}>→</span>
        </a>
        <a href="#" className={styles.linkItem}>
          <span className={styles.settingName}>개인정보처리방침</span>
          <span className={styles.linkArrow}>→</span>
        </a>
        <a href="#" className={styles.linkItem}>
          <span className={styles.settingName}>앱 버전</span>
          <span className={styles.linkArrow}>v1.0.0</span>
        </a>
      </div>

      <div className={styles.dangerSection}>
        <LogoutButton />
        <br />
        <button className={styles.dangerBtn}>계정 삭제</button>
      </div>
    </div>
  );
}
