import { Link, useLocation } from 'react-router';
import { 
  LayoutDashboard, 
  Package, 
  Calendar, 
  Settings, 
  MapPin, 
  MessageSquare,
  Crown,
  Users,
  Store,
  BarChart3,
  Wrench,
  DollarSign,
  FileText
} from 'lucide-react';
import type { UserRole } from '../../types';

interface SidebarProps {
  role: UserRole;
  isOpen: boolean;
  onClose?: () => void;
}

const menuItems = {
  CUSTOMER: [
    { icon: LayoutDashboard, label: 'Dashboard', path: '/customer/dashboard' },
    { icon: Package, label: 'My Orders', path: '/customer/orders' },
    { icon: Crown, label: 'Subscription', path: '/subscription/management' },
    { icon: MapPin, label: 'Saved Addresses', path: '/customer/addresses' },
    { icon: MessageSquare, label: 'Feedback', path: '/customer/feedback' },
    { icon: Settings, label: 'Settings', path: '/customer/settings' },
  ],
  ADMIN: [
    { icon: LayoutDashboard, label: 'Dashboard', path: '/admin/dashboard' },
    { icon: Users, label: 'User Management', path: '/admin/users' },
    { icon: Store, label: 'Shop Management', path: '/admin/shops' },
    { icon: Wrench, label: 'Service Config', path: '/admin/services' },
    { icon: Package, label: 'All Orders', path: '/admin/orders' },
    { icon: DollarSign, label: 'Revenue Analytics', path: '/admin/revenue' },
    { icon: MessageSquare, label: 'Feedback', path: '/admin/feedback' },
    { icon: Settings, label: 'System Settings', path: '/admin/settings' },
  ],
};

export function Sidebar({ role, isOpen, onClose }: SidebarProps) {
  const location = useLocation();
  const items = menuItems[role] || [];

  return (
    <>
      {/* Overlay for mobile */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed lg:sticky top-16 left-0 h-[calc(100vh-4rem)] w-64 bg-[#F3F4F6] border-r border-gray-200 transition-transform duration-300 z-40 ${
          isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        <nav className="p-4 space-y-1 overflow-y-auto h-full">
          {items.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;

            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={onClose}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 ${
                  isActive
                    ? 'bg-[#2563EB] text-white shadow-md'
                    : 'text-gray-700 hover:bg-white hover:shadow-sm'
                }`}
              >
                <Icon className="h-5 w-5" />
                <span className="text-sm">{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </aside>
    </>
  );
}
