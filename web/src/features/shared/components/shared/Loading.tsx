import { Loader2 } from 'lucide-react';

interface LoadingProps {
  text?: string;
  fullPage?: boolean;
}

export function Loading({ text = 'Loading...', fullPage = false }: LoadingProps) {
  const content = (
    <div className="flex flex-col items-center justify-center gap-3">
      <Loader2 className="h-8 w-8 animate-spin text-[#2563EB]" />
      {text && <p className="text-sm text-gray-600">{text}</p>}
    </div>
  );

  if (fullPage) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-white">
        {content}
      </div>
    );
  }

  return content;
}



