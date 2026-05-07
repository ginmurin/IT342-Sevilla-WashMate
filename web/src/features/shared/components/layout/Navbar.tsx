import { useState, useRef, useEffect } from 'react';
import { Bell, ChevronDown, Search, Menu, LogOut, Settings, HelpCircle, Droplet } from 'lucide-react';
import { useNavigate, Link } from 'react-router';
import { getCurrentUser, clearStoredAuth } from "@/features/shared/utils/auth";
import type { Notification } from "@/features/shared/types";

interface NavbarProps {
  onMenuClick?: () => void;
  showSearch?: boolean;
}

export function Navbar({ onMenuClick, showSearch = false }: NavbarProps) {
  const navigate = useNavigate();
  const user = getCurrentUser();
  const [showNotifications, setShowNotifications] = useState(false);
  const [showProfile, setShowProfile] = useState(false);
  const notifRef = useRef<HTMLDivElement>(null);
  const profileRef = useRef<HTMLDivElement>(null);

  const notifications: Notification[] = [
    {
      id: '1',
      title: 'Order Completed',
      message: 'Your order #12345 has been completed',
      time: '2 hours ago',
      read: false,
      icon: 'check',
    },
    {
      id: '2',
      title: 'Payment Received',
      message: 'Payment of ₱500 received',
      time: '5 hours ago',
      read: false,
      icon: 'money',
    },
  ];

  const unreadCount = notifications.filter(n => !n.read).length;

  const handleLogout = () => {
    clearStoredAuth();
    navigate('/login');
  };

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (notifRef.current && !notifRef.current.contains(event.target as Node)) {
        setShowNotifications(false);
      }
      if (profileRef.current && !profileRef.current.contains(event.target as Node)) {
        setShowProfile(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const getRoleBadgeColor = (role: string) => {
    switch (role) {
      case 'ADMIN':
        return 'bg-purple-100 text-purple-700';
      case 'SHOP_OWNER':
        return 'bg-[#14B8A6]/10 text-[#14B8A6]';
      default:
        return 'bg-[#2563EB]/10 text-[#2563EB]';
    }
  };

  const getRoleLabel = (role: string) => {
    switch (role) {
      case 'SHOP_OWNER':
        return 'Shop Owner';
      default:
        return role.charAt(0) + role.slice(1).toLowerCase();
    }
  };

  return (
    <nav className="h-16 bg-white border-b border-gray-200 px-4 lg:px-6 flex items-center justify-between sticky top-0 z-40">
      {/* Left section */}
      <div className="flex items-center gap-4">
        {onMenuClick && (
          <button
            onClick={onMenuClick}
            className="lg:hidden p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <Menu className="h-6 w-6 text-gray-600" />
          </button>
        )}
        
        <Link to="/" className="flex items-center gap-2 text-[#2563EB]">
          <Droplet className="h-8 w-8" />
          <span className="hidden sm:block text-xl">WashMate</span>
        </Link>
      </div>

      {/* Center - Search (optional) */}
      {showSearch && (
        <div className="hidden md:flex flex-1 max-w-md mx-8">
          <div className="relative w-full">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search..."
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#2563EB]/20 focus:border-[#2563EB]"
            />
          </div>
        </div>
      )}

      {/* Right section */}
      <div className="flex items-center gap-3">
        {/* Notifications */}
        <div className="relative" ref={notifRef}>
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="relative p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <Bell className="h-6 w-6 text-gray-600" />
            {unreadCount > 0 && (
              <span className="absolute top-1 right-1 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                {unreadCount}
              </span>
            )}
          </button>

          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg border border-gray-200 overflow-hidden">
              <div className="p-4 border-b border-gray-200 flex items-center justify-between">
                <h3 className="text-sm">Notifications</h3>
                <button className="text-xs text-[#2563EB] hover:text-[#1d4ed8]">
                  Mark all as read
                </button>
              </div>
              <div className="max-h-96 overflow-y-auto">
                {notifications.map((notif) => (
                  <div
                    key={notif.id}
                    className={`p-4 border-b border-gray-100 hover:bg-gray-50 cursor-pointer ${
                      !notif.read ? 'bg-blue-50/50' : ''
                    }`}
                  >
                    <div className="flex gap-3">
                      <div className="flex-shrink-0 w-2 h-2 mt-2 rounded-full bg-[#2563EB]" />
                      <div className="flex-1 min-w-0">
                        <p className="text-sm text-gray-900">{notif.title}</p>
                        <p className="text-sm text-gray-600 truncate">{notif.message}</p>
                        <p className="text-xs text-gray-400 mt-1">{notif.time}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
              <div className="p-3 text-center border-t border-gray-200">
                <button className="text-sm text-[#2563EB] hover:text-[#1d4ed8]">
                  View all notifications
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Profile Dropdown */}
        <div className="relative" ref={profileRef}>
          <button
            onClick={() => setShowProfile(!showProfile)}
            className="flex items-center gap-3 p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <div className="hidden sm:block text-right">
              <p className="text-sm">{user?.firstName} {user?.lastName}</p>
              <p className={`text-xs px-2 py-0.5 rounded-full inline-block ${getRoleBadgeColor(user?.role || '')}`}>
                {getRoleLabel(user?.role || '')}
              </p>
            </div>
            <div className="w-10 h-10 bg-gradient-to-br from-[#2563EB] to-[#14B8A6] rounded-full flex items-center justify-center text-white">
              {user?.firstName?.charAt(0) || 'U'}
            </div>
            <ChevronDown className="h-4 w-4 text-gray-600" />
          </button>

          {showProfile && (
            <div className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg border border-gray-200 overflow-hidden">
              <div className="p-4 border-b border-gray-200">
                <p className="text-sm">{user?.firstName} {user?.lastName}</p>
                <p className="text-xs text-gray-600">{user?.email}</p>
              </div>
              <div className="py-2">
                <button className="w-full px-4 py-2 text-left text-sm hover:bg-gray-100 flex items-center gap-3">
                  <Settings className="h-4 w-4 text-gray-600" />
                  Settings
                </button>
                <button className="w-full px-4 py-2 text-left text-sm hover:bg-gray-100 flex items-center gap-3">
                  <HelpCircle className="h-4 w-4 text-gray-600" />
                  Help & Support
                </button>
              </div>
              <div className="border-t border-gray-200">
                <button
                  onClick={handleLogout}
                  className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center gap-3"
                >
                  <LogOut className="h-4 w-4" />
                  Logout
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}



