import styles from './page.module.css';
import ResultClient from './_components/ResultClient';

type PageProps = {
  searchParams?: Promise<Record<string, string | string[] | undefined>>;
};

export default async function TryonResultPage({ searchParams }: PageProps) {
  const resolved = (await searchParams) ?? {};
  const jobId = typeof resolved.job_id === 'string' ? resolved.job_id : undefined;
  const resultImageUrl = typeof resolved.result_image_url === 'string' ? resolved.result_image_url : undefined;
  return <ResultClient className={styles.resultPage} jobId={jobId} resultImageUrl={resultImageUrl} />;
}
