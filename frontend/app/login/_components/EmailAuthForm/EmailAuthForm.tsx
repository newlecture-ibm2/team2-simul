'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './EmailAuthForm.module.css';
import { useAuth } from '../useAuth';
import ConfirmModal from '@/components/ConfirmModal/ConfirmModal';

interface EmailAuthFormProps {
  type: 'login' | 'signup';
}

export default function EmailAuthForm({ type }: EmailAuthFormProps) {
  const router = useRouter();
  const { 
    loginWithEmail, 
    signupWithEmail, 
    handleRestore,
    isLoading,
    isRestoreModalOpen,
    setIsRestoreModalOpen,
    restoreMessage,
    authMode
  } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [nickname, setNickname] = useState('');
  const [gender, setGender] = useState<'MALE' | 'FEMALE' | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (type === 'signup' && !gender) {
      alert('성별을 선택해주세요.');
      return;
    }

    if (type === 'signup') {
      signupWithEmail({ email, password, name, nickname, gender });
    } else {
      loginWithEmail({ email, password });
    }
  };

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <div className={styles.inputGroup}>
        <input 
          type="email" 
          placeholder="이메일 주소" 
          className={styles.input}
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
      </div>
      <div className={styles.inputGroup}>
        <input 
          type="password" 
          placeholder="비밀번호" 
          className={styles.input}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
      </div>
      
      {type === 'signup' && (
        <>
          <div className={styles.inputGroup}>
            <input 
              type="password" 
              placeholder="비밀번호 확인" 
              className={styles.input}
              required
            />
          </div>
          <div className={styles.inputGroup}>
            <input 
              type="text" 
              placeholder="이름" 
              className={styles.input}
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <div className={styles.inputGroup}>
            <input 
              type="text" 
              placeholder="닉네임" 
              className={styles.input}
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              required
            />
          </div>
          <div className={styles.inputGroup}>
            <span className={styles.label}>성별</span>
            <div className={styles.genderGroup}>
              <button
                type="button"
                className={`${styles.genderBtn} ${gender === 'MALE' ? styles.active : ''}`}
                onClick={() => setGender('MALE')}
              >
                남성
              </button>
              <button
                type="button"
                className={`${styles.genderBtn} ${gender === 'FEMALE' ? styles.active : ''}`}
                onClick={() => setGender('FEMALE')}
              >
                여성
              </button>
            </div>
          </div>
          <p className={styles.privacyNote}>
            가입을 진행하면 SIMUL의 <a href="#">이용약관</a> 및 <a href="#">개인정보처리방침</a>에 동의하게 됩니다.
          </p>
        </>
      )}

      <button className={styles.submitBtn} disabled={isLoading}>
        {isLoading ? '처리 중...' : type === 'login' ? '로그인' : '회원가입 완료'}
      </button>

      {type === 'login' && (
        <button 
          type="button" 
          className={styles.forgotPassword} 
          onClick={() => router.push('/login/find-password')}
        >
          비밀번호를 잊으셨나요?
        </button>
      )}

      <ConfirmModal
        isOpen={isRestoreModalOpen}
        title={authMode === 'login' ? '계정 복구 안내' : '가입 안내'}
        description={restoreMessage}
        confirmText={authMode === 'login' ? '복구하기' : '확인'}
        cancelText={authMode === 'login' ? '아니오' : ''}
        onConfirm={authMode === 'login' ? handleRestore : () => {
          setIsRestoreModalOpen(false);
          router.push('/');
        }}
        onCancel={() => {
          setIsRestoreModalOpen(false);
          // 로그인 복구 모달일 때만 '아니오'나 닫기를 누르면 메인으로 이동
          if (authMode === 'login') {
            router.push('/');
          }
        }}
      />
    </form>
  );
}
