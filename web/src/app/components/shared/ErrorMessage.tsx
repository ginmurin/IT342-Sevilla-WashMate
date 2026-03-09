import { AlertCircle, X } from 'lucide-react';
import { useState } from 'react';

interface ErrorMessageProps {
  message: string;
  dismissible?: boolean;
  onDismiss?: () => void;
}

export function ErrorMessage({ message, dismissible = false, onDismiss }: ErrorMessageProps) {
  const [visible, setVisible] = useState(true);

  const handleDismiss = () => {
    setVisible(false);
    onDismiss?.();
  };

  if (!visible) return null;

  return (
    <div className="flex items-start gap-3 rounded-lg border border-red-300 bg-red-50 p-4">
      <AlertCircle className="h-5 w-5 flex-shrink-0 text-red-600" />
      <p className="flex-1 text-sm text-red-800">{message}</p>
      {dismissible && (
        <button
          onClick={handleDismiss}
          className="flex-shrink-0 text-red-600 hover:text-red-800 transition-colors"
        >
          <X className="h-4 w-4" />
        </button>
      )}
    </div>
  );
}
