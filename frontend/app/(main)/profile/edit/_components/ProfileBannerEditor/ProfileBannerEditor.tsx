'use client';

import { useRef, useState, useEffect } from 'react';
import styles from './ProfileBannerEditor.module.css';

interface ProfileBannerEditorProps {
  currentImageUrl?: string;
  onImageSelect: (file: File) => void;
}

export default function ProfileBannerEditor({ currentImageUrl, onImageSelect }: ProfileBannerEditorProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

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
      if (previewUrl) URL.revokeObjectURL(previewUrl);
      const url = URL.createObjectURL(file);
      setPreviewUrl(url);
      onImageSelect(file);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.bannerWrapper} onClick={handleClick}>
        <div className={styles.banner}>
          {(previewUrl || currentImageUrl) ? (
            <img src={previewUrl || currentImageUrl} alt="Banner" />
          ) : (
            <div className={styles.placeholder}>배너 이미지를 등록해주세요</div>
          )}
        </div>
        <div className={styles.overlay}>
          <span className={styles.cameraIcon}>📸 배경 사진 변경</span>
        </div>
      </div>
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
