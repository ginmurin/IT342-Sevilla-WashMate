import { useState } from 'react';
import { Navbar } from './Navbar';
import { Sidebar } from './Sidebar';
import type { UserRole } from '../../types';

interface DashboardLayoutProps {
  role: UserRole;
  children: React.ReactNode;
  showSearch?: boolean;
}

export function DashboardLayout({ role, children, showSearch = false }: DashboardLayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="min-h-screen bg-[#F3F4F6]">
      <Navbar 
        onMenuClick={() => setSidebarOpen(!sidebarOpen)} 
        showSearch={showSearch}
      />
      
      <div className="flex">
        <Sidebar 
          role={role} 
          isOpen={sidebarOpen} 
          onClose={() => setSidebarOpen(false)} 
        />
        
        <main className="flex-1 p-6 lg:p-8">
          {children}
        </main>
      </div>
    </div>
  );
}
