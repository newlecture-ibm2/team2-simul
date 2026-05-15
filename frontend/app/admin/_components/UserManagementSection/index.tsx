'use client';

import React, { useState } from 'react';
import styles from './UserManagementSection.module.css';
import { useUserManagement } from './useUserManagement';
import ConfirmModal from '@/components/ConfirmModal';
import PromptModal from '../PromptModal';

export default function UserManagementSection() {
  const { users, isLoading, suspendUser, provideCredits } = useUserManagement();
  
  const [confirmConfig, setConfirmConfig] = useState<{
    isOpen: boolean;
    title: string;
    description: string;
    onConfirm: () => void;
  }>({
    isOpen: false,
    title: '',
    description: '',
    onConfirm: () => {},
  });

  const openConfirm = (title: string, description: string, onConfirm: () => void) => {
    setConfirmConfig({ isOpen: true, title, description, onConfirm });
  };

  const closeConfirm = () => {
    setConfirmConfig(prev => ({ ...prev, isOpen: false }));
  };

  const [promptConfig, setPromptConfig] = useState<{
    isOpen: boolean;
    title: string;
    description: string;
    onConfirm: (value: string) => void;
  }>({
    isOpen: false,
    title: '',
    description: '',
    onConfirm: () => {},
  });

  const openPrompt = (title: string, description: string, onConfirm: (value: string) => void) => {
    setPromptConfig({ isOpen: true, title, description, onConfirm });
  };

  const closePrompt = () => {
    setPromptConfig(prev => ({ ...prev, isOpen: false }));
  };

  if (isLoading) {
    return <div className={styles.empty}>로딩 중...</div>;
  }

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>유저 관리</h2>
      
      {users.length === 0 ? (
        <div className={styles.empty}>가입된 유저가 없습니다.</div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>유저 ID</th>
                <th>닉네임</th>
                <th>가입 유형</th>
                <th>계정 이메일</th>
                <th>역할</th>
                <th>상태</th>
                <th>가입일</th>
                <th>계정 정지</th>
                <th>크레딧 지급</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.userId}>
                  <td data-label="유저 ID">{user.userId.substring(0, 8)}...</td>
                  <td data-label="닉네임">{user.nickname}</td>
                  <td data-label="가입 유형">{user.provider}</td>
                  <td data-label="계정 이메일">{user.providerId}</td>
                  <td data-label="역할">{user.role}</td>
                  <td data-label="상태">
                    <span style={{ color: user.isActive ? 'var(--color-green-hover)' : '#ff3b30', fontWeight: 600 }}>
                      {user.isActive ? '정상' : '정지됨'}
                    </span>
                  </td>
                  <td data-label="가입일">{new Date(user.createdAt).toLocaleDateString()}</td>
                  <td data-label="계정 정지">
                      <button 
                        className={styles.btnDanger}
                        disabled={!user.isActive}
                        onClick={() => openConfirm('유저 정지', `'${user.nickname}' 유저를 정지 처리하시겠습니까?`, () => {
                          suspendUser(user.userId); 
                          closeConfirm();
                        })}
                      >
                        유저 정지
                      </button>
                  </td>
                  <td data-label="크레딧 지급">
                      <button 
                        className={styles.btnPrimary}
                        disabled={!user.isActive}
                        onClick={() => openPrompt('크레딧 지급', `'${user.nickname}' 유저에게 지급할 크레딧 수량을 입력하세요.\n(현재 백엔드는 이 수량을 사용하지 않고 5회로 초기화합니다.)`, (value) => {
                          const amount = parseInt(value, 10);
                          if (!isNaN(amount) && amount > 0) {
                            provideCredits({ userId: user.userId, amount });
                            closePrompt();
                          }
                        })}
                      >
                        크레딧 지급
                      </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmModal
        isOpen={confirmConfig.isOpen}
        title={confirmConfig.title}
        description={confirmConfig.description}
        onConfirm={confirmConfig.onConfirm}
        onCancel={closeConfirm}
      />
      <PromptModal
        isOpen={promptConfig.isOpen}
        title={promptConfig.title}
        description={promptConfig.description}
        type="number"
        placeholder="예: 5"
        onConfirm={promptConfig.onConfirm}
        onCancel={closePrompt}
      />
    </div>
  );
}
