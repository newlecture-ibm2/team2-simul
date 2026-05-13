'use client';

import { useRef, useState, useEffect } from 'react';
import styles from './ProfileImageEditor.module.css';

interface ProfileImageEditorProps {
  currentImageUrl?: string;
  onImageSelect: (file: File) => void;
}

export default function ProfileImageEditor({ currentImageUrl, onImageSelect }: ProfileImageEditorProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  // 컴포넌트 언마운트 시 메모리 누수 방지를 위해 미리보기 URL 해제
  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // 기존 미리보기 URL 해제
      if (previewUrl) URL.revokeObjectURL(previewUrl);
      
      const url = URL.createObjectURL(file);
      setPreviewUrl(url);
      onImageSelect(file);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.avatarWrapper} onClick={handleClick}>
        <div className={styles.avatar}>
          {(previewUrl || currentImageUrl) ? (
            <img src={previewUrl || currentImageUrl} alt="Profile" />
          ) : (
            <span className={styles.defaultIcon}>🧑</span>
          )}
        </div>
        <div className={styles.overlay}>
          <span className={styles.cameraIcon}>📷</span>
        </div>
      </div>
      <button type="button" className={styles.changeBtn} onClick={handleClick}>
        프로필 사진 변경
      </button>
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        className={styles.hiddenInput}
        onChange={handleFileChange}
      />
    </div>
  );
}
