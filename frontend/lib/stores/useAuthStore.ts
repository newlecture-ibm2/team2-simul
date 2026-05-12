import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface User {
  userId: string;
  id?: string; // develop에서 사용된 id 호환용
  nickname: string;
  name?: string;
  email?: string;
  profileImageUrl?: string;
  bio?: string;
  role?: string;
  followerCount?: number;
  followingCount?: number;
  isFollowing?: boolean;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  setUser: (user: User | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      setUser: (user) => set({ user, isAuthenticated: !!user }),
      logout: () => set({ user: null, isAuthenticated: false }),
    }),
    {
      name: 'auth-storage',
    }
  )
);
