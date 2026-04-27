import { create } from 'zustand';

interface UIState {
  isMobileMenuOpen: boolean;
  isModalOpen: boolean;
  modalContent: string | null;
  toggleMobileMenu: () => void;
  openModal: (content: string) => void;
  closeModal: () => void;
}

export const useUIStore = create<UIState>((set) => ({
  isMobileMenuOpen: false,
  isModalOpen: false,
  modalContent: null,
  toggleMobileMenu: () =>
    set((state) => ({ isMobileMenuOpen: !state.isMobileMenuOpen })),
  openModal: (content) => set({ isModalOpen: true, modalContent: content }),
  closeModal: () => set({ isModalOpen: false, modalContent: null }),
}));
