import styles from './page.module.css';
import ProcessingClient from './_components/ProcessingClient';

export const metadata = {
  title: '시착 생성 중 — SIMUL',
  description: 'AI가 가상시착 결과를 생성하고 있습니다.',
};

type PageProps = {
  searchParams?: Promise<Record<string, string | string[] | undefined>>;
};

export default async function ProcessingPage({ searchParams }: PageProps) {
  const resolved = (await searchParams) ?? {};
  const jobId = typeof resolved.job_id === 'string' ? resolved.job_id : '';
  return <ProcessingClient className={styles.processing} styles={styles} jobId={jobId} />;
}
