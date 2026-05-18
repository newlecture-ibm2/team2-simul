import styles from './page.module.css';
import TryonHomeClient from './_components/TryonHomeClient/TryonHomeClient';

export const metadata = {
  title: '가상시착 — SIMUL',
  description: 'AI 가상시착을 시작하세요. 내 사진에 옷을 입혀보세요.',
};

export default function TryonHomePage() {
  return <TryonHomeClient styles={styles} />;
}
