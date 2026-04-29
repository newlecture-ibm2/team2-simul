'use client';

import { useState } from 'react';
import styles from './EmailAuthForm.module.css';
import { useAuth } from '../useAuth';

interface EmailAuthFormProps {
  type: 'login' | 'signup';
}

export default function EmailAuthForm({ type }: EmailAuthFormProps) {
  const { login, isLoading } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // 실제로는 여기서 이메일 로그인/회원가입 로직이 실행됩니다.
    login('google'); // Mock 로직 재사용
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
        <div className={styles.inputGroup}>
          <input 
            type="password" 
            placeholder="비밀번호 확인" 
            className={styles.input}
            required
          />
        </div>
      )}

      <button className={styles.submitBtn} disabled={isLoading}>
        {isLoading ? '처리 중...' : type === 'login' ? '로그인' : '회원가입 완료'}
      </button>

      {type === 'login' && (
        <a href="#" className={styles.forgotPassword}>비밀번호를 잊으셨나요?</a>
      )}
    </form>
  );
}
