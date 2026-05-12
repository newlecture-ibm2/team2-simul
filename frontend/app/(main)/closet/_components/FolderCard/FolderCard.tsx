'use client';

import { useState, useRef, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import styles from './FolderCard.module.css';
import ConfirmModal from '../ConfirmModal/ConfirmModal';

interface FolderCardProps {
  id: string | number;
  title: string;
  itemCount: number;
  lastUpdated: string;
  images: string[];
  onClick?: (id: string | number) => void;
  isEditing?: boolean;
  onRename?: (id: string | number, newTitle: string) => void;
  onDelete?: (id: string | number) => void;
}

export default function FolderCard({
  id,
  title,
  itemCount,
  lastUpdated,
  images,
  onClick,
  isEditing = false,
  onRename,
  onDelete
}: FolderCardProps) {
  const router = useRouter();
  const [showMenu, setShowMenu] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const mainImage = images[0];
  const subImage1 = images[1];
  const subImage2 = images[2];

  const [tempTitle, setTempTitle] = useState(title);

  // Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setShowMenu(false);
      }
    };
    if (showMenu) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showMenu]);

  const handleEditClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    setShowMenu(false);
    router.push(`/closet/folders/${id}?edit=true`);
  };

  const handleDeleteClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    setShowMenu(false);
    setShowDeleteConfirm(true);
  };

  const confirmDelete = () => {
    setShowDeleteConfirm(false);
    onDelete?.(id);
  };

  return (
    <div className={styles.folderCard} onClick={() => !isEditing && onClick?.(id)}>
      <div className={styles.imageGrid}>
        <div className={styles.mainImageWrapper}>
          {mainImage && <img src={mainImage} alt={`${title} 메인`} className={styles.image} />}
        </div>
        <div className={styles.subImagesWrapper}>
          <div className={styles.subImageWrapper}>
            {subImage1 && <img src={subImage1} alt={`${title} 서브1`} className={styles.image} />}
          </div>
          <div className={styles.subImageWrapper}>
            {subImage2 && <img src={subImage2} alt={`${title} 서브2`} className={styles.image} />}
          </div>
        </div>
      </div>
      
      <div className={styles.info}>
        <div className={styles.infoTop}>
          {isEditing ? (
            <input
              className={styles.titleInput}
              value={tempTitle}
              onChange={(e) => setTempTitle(e.target.value)}
              onBlur={() => onRename?.(id, tempTitle)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') onRename?.(id, tempTitle);
              }}
              autoFocus
              onClick={(e) => e.stopPropagation()}
            />
          ) : (
            <h3 className={styles.title}>{title}</h3>
          )}
          
          {!isEditing && (
            <div className={styles.menuContainer} ref={menuRef}>
              <button 
                className={styles.ellipsisBtn}
                onClick={(e) => {
                  e.stopPropagation();
                  setShowMenu(!showMenu);
                }}
                aria-label="폴더 메뉴"
              >
                <img src="/icons/ellipsis.png" alt="더보기" className={styles.ellipsisIcon} />
              </button>

              {showMenu && (
                <div className={styles.dropdownMenu}>
                  <button className={styles.menuItem} onClick={handleEditClick}>
                    폴더 수정
                  </button>
                  <button className={`${styles.menuItem} ${styles.deleteItem}`} onClick={handleDeleteClick}>
                    폴더 삭제
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
        <p className={styles.meta}>핀 {itemCount}개 · {lastUpdated}</p>
      </div>

      <ConfirmModal
        isOpen={showDeleteConfirm}
        title="폴더를 삭제하시겠습니까?"
        description={`'${title}' 폴더와 그 안의 정보가 삭제됩니다.\n(폴더 안의 실제 옷 아이템은 삭제되지 않습니다.)`}
        confirmText="삭제"
        cancelText="취소"
        isDestructive={true}
        onConfirm={confirmDelete}
        onCancel={() => setShowDeleteConfirm(false)}
      />
    </div>
  );
}
