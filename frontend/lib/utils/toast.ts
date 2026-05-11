export type ToastType = 'success' | 'error' | 'info';

export interface ToastPayload {
  message: string;
  type: ToastType;
}

const TOAST_EVENT_NAME = 'simul_toast_event';

export const toast = {
  success: (message: string) => {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(
        new CustomEvent<ToastPayload>(TOAST_EVENT_NAME, {
          detail: { message, type: 'success' },
        })
      );
    }
  },
  error: (message: string) => {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(
        new CustomEvent<ToastPayload>(TOAST_EVENT_NAME, {
          detail: { message, type: 'error' },
        })
      );
    }
  },
  info: (message: string) => {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(
        new CustomEvent<ToastPayload>(TOAST_EVENT_NAME, {
          detail: { message, type: 'info' },
        })
      );
    }
  },
  listen: (callback: (payload: ToastPayload) => void) => {
    if (typeof window === 'undefined') return () => {};

    const handler = (e: Event) => {
      const customEvent = e as CustomEvent<ToastPayload>;
      callback(customEvent.detail);
    };

    window.addEventListener(TOAST_EVENT_NAME, handler);
    return () => window.removeEventListener(TOAST_EVENT_NAME, handler);
  },
};
