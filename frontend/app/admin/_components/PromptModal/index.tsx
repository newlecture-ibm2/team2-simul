'use client';

import React, { useState, useEffect } from 'react';
import styles from './PromptModal.module.css';

interface PromptModalProps {
  isOpen: boolean;
  title: string;
  description?: string;
  placeholder?: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: (value: string) => void;
  onCancel: () => void;
  type?: 'text' | 'number';
}

export default function PromptModal({
  isOpen,
  title,
  description,
  placeholder = '',
  confirmText = '확인',
  cancelText = '취소',
  onConfirm,
  onCancel,
  type = 'text',
}: PromptModalProps) {
  const [inputValue, setInputValue] = useState('');

  useEffect(() => {
    if (isOpen) {
      setInputValue(''); // Reset on open
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onCancel}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <h2 className={styles.title}>{title}</h2>
        {description && <p className={styles.description}>{description}</p>}
        
        <input
          type={type}
          className={styles.input}
          placeholder={placeholder}
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          autoFocus
        />

        <div className={styles.actions}>
          <button className={`${styles.btn} ${styles.cancelBtn}`} onClick={onCancel}>
            {cancelText}
          </button>
          <button
            className={`${styles.btn} ${styles.confirmBtn}`}
            onClick={() => onConfirm(inputValue)}
            disabled={!inputValue.trim()}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}
