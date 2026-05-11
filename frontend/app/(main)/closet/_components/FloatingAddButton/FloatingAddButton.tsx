'use client';

import { createPortal } from 'react-dom';
import styles from './FloatingAddButton.module.css';

interface FloatingAddButtonProps {
  onClick?: () => void;
  ariaLabel?: string;
}

export default function FloatingAddButton({ 
  onClick, 
  ariaLabel = "추가" 
}: FloatingAddButtonProps) {
  if (typeof window === 'undefined') return null;

  return createPortal(
    <button 
      className={styles.floatingAddBtn} 
      onClick={onClick}
      aria-label={ariaLabel}
    >
      <span className={styles.plusIcon}>+</span>
    </button>,
    document.body
  );
}
