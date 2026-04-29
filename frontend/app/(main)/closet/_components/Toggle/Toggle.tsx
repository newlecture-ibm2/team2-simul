import styles from './Toggle.module.css';

export interface ToggleOption {
  id: string;
  label?: string;
  icon?: string;
}

interface ToggleProps {
  options: ToggleOption[];
  activeId: string;
  onChange: (id: string) => void;
  className?: string;
  size?: 'small' | 'medium';
}

export default function Toggle({ 
  options, 
  activeId, 
  onChange, 
  className = '',
  size = 'medium' 
}: ToggleProps) {
  return (
    <div className={`${styles.toggleTrack} ${styles[size]} ${className}`}>
      {options.map((option) => {
        const isActive = activeId === option.id;
        return (
          <button
            key={option.id}
            className={`${styles.toggleBtn} ${isActive ? styles.activeToggle : ''}`}
            onClick={() => onChange(option.id)}
          >
            {option.icon ? (
              <img src={option.icon} alt={option.label || option.id} className={styles.toggleIcon} />
            ) : (
              <span className={styles.toggleLabel}>{option.label}</span>
            )}
          </button>
        );
      })}
    </div>
  );
}
