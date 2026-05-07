import { CheckCircle2 } from 'lucide-react';
import { useEffect, useState } from 'react';

interface SuccessMessageProps {
  message: string;
  autoDismiss?: boolean;
  duration?: number;
}

export function SuccessMessage({ message, autoDismiss = true, duration = 3000 }: SuccessMessageProps) {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    if (autoDismiss) {
      const timer = setTimeout(() => {
        setVisible(false);
      }, duration);
      return () => clearTimeout(timer);
    }
  }, [autoDismiss, duration]);

  if (!visible) return null;

  return (
    <div className="flex items-start gap-3 rounded-lg border border-green-300 bg-green-50 p-4">
      <CheckCircle2 className="h-5 w-5 flex-shrink-0 text-green-600" />
      <p className="flex-1 text-sm text-green-800">{message}</p>
    </div>
  );
}



