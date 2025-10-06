import React, { createContext, useContext, useState, ReactNode, useCallback } from 'react';
import Toast, { ToastProps } from '../components/Toast';

interface ToastContextType {
  showToast: (message: string, type: ToastProps['type']) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Array<{ id: number; message: string; type: ToastProps['type'] }>>([]);
  const [nextId, setNextId] = useState(0);

  const showToast = useCallback((message: string, type: ToastProps['type']) => {
    const id = nextId;
    setNextId(id + 1);
    setToasts((prev) => [...prev, { id, message, type }]);
  }, [nextId]);

  const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
        {toasts.map((toast) => (
          <Toast
            key={toast.id}
            message={toast.message}
            type={toast.type}
            onClose={() => removeToast(toast.id)}
          />
        ))}
      </div>
    </ToastContext.Provider>
  );
};

// Custom hook to use ToastContext
// eslint-disable-next-line react-refresh/only-export-components
export const useToast = () => {
  const context = useContext(ToastContext);
  if (context === undefined) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

