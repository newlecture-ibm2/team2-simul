'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { withdraw } from '@/lib/api/authAPI';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import { toast } from '@/lib/utils/toast';
import ConfirmModal from '@/components/ConfirmModal';
import styles from './WithdrawButton.module.css';

export default function WithdrawButton() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isPending, setIsPending] = useState(false);
  const router = useRouter();
  const logout = useAuthStore((state) => state.logout);

  const handleWithdraw = async () => {
    setIsPending(true);
    try {
      await withdraw();
      toast.success('회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.');
      
      // Zustand 상태 초기화 및 홈으로 이동
      logout();
      router.push('/login');
    } catch (error) {
      console.error('Withdraw failed:', error);
      const err = error as { message?: string };
      toast.error(err.message || '탈퇴 처리 중 오류가 발생했습니다.');
    } finally {
      setIsPending(false);
      setIsModalOpen(false);
    }
  };

  return (
    <>
      <button 
        className={styles.withdrawBtn} 
        onClick={() => setIsModalOpen(true)}
        disabled={isPending}
      >
        {isPending ? '처리 중...' : '탈퇴하기'}
      </button>

      <ConfirmModal
        isOpen={isModalOpen}
        title="회원 탈퇴"
        description="정말로 탈퇴하시겠습니까? 탈퇴 시 모든 데이터는 복구가 불가능하며, 작성하신 피드와 등록한 옷장 정보가 완전히 삭제됩니다."
        confirmText="탈퇴하기"
        cancelText="취소"
        onConfirm={handleWithdraw}
        onCancel={() => setIsModalOpen(false)}
        isDestructive={true}
      />
    </>
  );
}
