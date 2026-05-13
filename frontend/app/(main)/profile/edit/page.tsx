'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCurrentUser, updateProfile } from '@/lib/api/authAPI';
import { User } from '@/lib/stores/useAuthStore';
import Button from '@/components/Button';
import ProfileImageEditor from './_components/ProfileImageEditor';
import ProfileBannerEditor from './_components/ProfileBannerEditor';
import styles from './page.module.css';

const MAX_NICKNAME = 30;
const MAX_BIO = 150;

export default function ProfileEditPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [nickname, setNickname] = useState('');
  const [bio, setBio] = useState('');
  const [selectedImage, setSelectedImage] = useState<File | null>(null);
  const [selectedBanner, setSelectedBanner] = useState<File | null>(null);

  // 1. 기존 정보 불러오기
  const { data: user, isLoading } = useQuery<User>({
    queryKey: ['me'],
    queryFn: getCurrentUser,
  });

  // 2. 초기값 설정
  useEffect(() => {
    if (user) {
      setNickname(user.nickname || '');
      setBio(user.bio || '');
    }
  }, [user]);

  // 3. 수정 요청 (Mutation)
  const mutation = useMutation({
    mutationFn: updateProfile,
    onSuccess: () => {
      // 캐시 갱신하여 다른 페이지에도 반영되게 함
      queryClient.invalidateQueries({ queryKey: ['me'] });
      alert('프로필이 수정되었습니다!');
      router.push('/profile');
    },
    onError: (error: Error) => {
      alert(error.message || '수정 중 오류가 발생했습니다.');
    }
  });

  const handleSave = () => {
    mutation.mutate({ 
      nickname, 
      bio, 
      profileImage: selectedImage || undefined,
      bannerImage: selectedBanner || undefined
    });
  };

  if (isLoading) return <div className={styles.loading}>로딩 중...</div>;

  return (
    <div className={styles.editPage}>
      <h1>프로필 편집</h1>

      <ProfileImageEditor 
        currentImageUrl={user?.profileImageUrl} 
        onImageSelect={setSelectedImage} 
      />

      <ProfileBannerEditor 
        currentImageUrl={user?.bannerImageUrl}
        onImageSelect={setSelectedBanner}
      />

      <div className={styles.formGroup}>
        <div className={styles.labelRow}>
          <label htmlFor="nickname">닉네임</label>
          <span className={`${styles.counter} ${nickname.length >= MAX_NICKNAME ? styles.counterMax : ''}`}>
            {nickname.length}/{MAX_NICKNAME}
          </span>
        </div>
        <input
          id="nickname"
          type="text"
          className={styles.input}
          value={nickname}
          onChange={(e) => setNickname(e.target.value.slice(0, MAX_NICKNAME))}
          placeholder="닉네임을 입력하세요"
        />
      </div>

      <div className={styles.formGroup}>
        <div className={styles.labelRow}>
          <label htmlFor="bio">한줄 소개</label>
          <span className={`${styles.counter} ${bio.length >= MAX_BIO ? styles.counterMax : ''}`}>
            {bio.length}/{MAX_BIO}
          </span>
        </div>
        <textarea
          id="bio"
          className={styles.textarea}
          value={bio}
          onChange={(e) => setBio(e.target.value.slice(0, MAX_BIO))}
          placeholder="자기소개를 입력하세요"
          rows={4}
        />
      </div>

      <div className={styles.submitRow}>
        <Button 
          variant="secondary" 
          size="lg" 
          fullWidth 
          onClick={() => router.back()}
          disabled={mutation.isPending}
        >
          취소
        </Button>
        <Button 
          variant="primary" 
          size="lg" 
          fullWidth 
          onClick={handleSave}
          disabled={mutation.isPending}
        >
          {mutation.isPending ? '저장 중...' : '저장'}
        </Button>
      </div>
    </div>
  );
}
