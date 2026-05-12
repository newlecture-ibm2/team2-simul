'use client';

import { useParams } from 'next/navigation';
import FollowListPage from '../../_components/FollowListPage/FollowListPage';

export default function UserFollowingsPage() {
  const params = useParams();
  const userId = params.id as string;

  return <FollowListPage userId={userId} type="followings" />;
}
