import Link from 'next/link';
import styles from './page.module.css';
import MainToggle from './_components/MainToggle';
import FeedGrid from './_components/FeedGrid';
import SwipeDeck from './_components/SwipeDeck/SwipeDeck';

export const metadata = {
  title: '홈 피드 — SIMUL',
  description: 'AI 가상시착 결과와 패션 게시물을 탐색하세요.',
};

export default function HomePage() {
  return (
    <div className={styles.homePage}>
      <SwipeDeck />
      <MainToggle />
      <FeedGrid />
    </div>
  );
}
