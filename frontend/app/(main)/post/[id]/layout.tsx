import { Metadata, ResolvingMetadata } from 'next';

type Props = {
  params: Promise<{ id: string }>;
  children: React.ReactNode;
};

// Next.js 15+ App Router Server Component
export async function generateMetadata(
  { params }: Props,
  parent: ResolvingMetadata
): Promise<Metadata> {
  const resolvedParams = await params;
  const id = resolvedParams.id;
  
  // 백엔드 URL로 직접 데이터를 페칭합니다. (Next.js 서버 사이드)
  const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';
  const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL || 'http://localhost:3000';

  try {
    const res = await fetch(`${BACKEND_URL}/posts/${id}`, { cache: 'no-store' });
    if (!res.ok) {
      return {
        title: '게시물 | SIMUL',
      };
    }

    const post = await res.json();
    
    // 캡션 말줄임 처리 (최대 50자)
    const description = post.caption
      ? post.caption.length > 50
        ? `${post.caption.slice(0, 50)}...`
        : post.caption
      : '이 코디 어때요?';

    // 대표 이미지 URL (절대 경로로 변환)
    const imageUrl = post.images && post.images.length > 0
      ? `${SITE_URL}${post.images[0].imageUrl}`
      : `${SITE_URL}/icons/icon-192x192.png`; // Fallback image

    return {
      title: `[SIMUL] ${post.nickname}님의 스타일`,
      description: description,
      openGraph: {
        title: `[SIMUL] ${post.nickname}님의 스타일`,
        description: description,
        url: `${SITE_URL}/post/${id}`,
        type: 'article',
        images: [
          {
            url: imageUrl,
            width: 800,
            height: 1200,
            alt: 'SIMUL 패션 코디',
          },
        ],
      },
      twitter: {
        card: 'summary_large_image',
        title: `[SIMUL] ${post.nickname}님의 스타일`,
        description: description,
        images: [imageUrl],
      },
    };
  } catch (error) {
    console.error('[generateMetadata] Failed to fetch post:', error);
    return {
      title: '게시물 | SIMUL',
    };
  }
}

export default function PostLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}
