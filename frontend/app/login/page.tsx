'use client';

import { useState } from 'react';
import styles from './page.module.css';
import SocialLoginButtons from './_components/SocialLoginButtons';
import EmailAuthForm from './_components/EmailAuthForm/EmailAuthForm';

export default function LoginPage() {
  const [authMode, setAuthMode] = useState<'social' | 'email'>('social');
  const [authType, setAuthType] = useState<'login' | 'signup'>('login');

  return (
    <div className={styles.loginPage}>
      {/* 배경 이미지 레이어 */}
      <div className={styles.backgroundLayer} />

      <div className={styles.loginContainer}>
        <div className={styles.loginCard}>
          <div className={styles.brandSection}>
            <div className={styles.logoMark}>S</div>
            <h1 className={styles.brandName}>SIMUL</h1>
            <p className={styles.brandTagline}>
              AI 가상시착으로 나만의 스타일을 발견하세요
            </p>
          </div>

          <div className={styles.tabSection}>
            <button
              className={`${styles.tabBtn} ${authMode === 'social' ? styles.activeTab : ''}`}
              onClick={() => setAuthMode('social')}
            >
              소셜 계정
            </button>
            <button
              className={`${styles.tabBtn} ${authMode === 'email' ? styles.activeTab : ''}`}
              onClick={() => setAuthMode('email')}
            >
              이메일
            </button>
          </div>

          <div className={styles.contentSection}>
            {authMode === 'social' ? (
              <SocialLoginButtons />
            ) : (
              <>
                <h2 className={styles.formTitle}>
                  {authType === 'login' ? '이메일 로그인' : '이메일 회원가입'}
                </h2>
                <EmailAuthForm type={authType} />
                <button
                  className={styles.switchTypeBtn}
                  onClick={() => setAuthType(authType === 'login' ? 'signup' : 'login')}
                >
                  {authType === 'login' ? '계정이 없으신가요? 회원가입' : '이미 계정이 있으신가요? 로그인'}
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
