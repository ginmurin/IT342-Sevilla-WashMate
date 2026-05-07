import { useEffect, useState, useMemo } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import {
  Users,
  Search,
  ChevronDown,
  UserCheck,
  UserX,
  Trash2,
  Shield,
  MoreVertical,
  Loader2,
  AlertCircle,
  Mail,
  Phone,
  Calendar,
  Filter,
  Wallet,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import { adminAPI } from "../utils/api";
import type { User } from "../types";
import { useAuth } from "../contexts/AuthContext";

export default function AdminUsers() {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [updatingUser, setUpdatingUser] = useState<number | null>(null);
  
  // Wallet Modal State
  const [isWalletModalOpen, setIsWalletModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [newBalance, setNewBalance] = useState("");

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await adminAPI.getAllUsers();
      setUsers(data);
    } catch (err) {
      console.error("Failed to load users:", err);
      setError("Failed to load users list");
    } finally {
      setIsLoading(false);
    }
  };

  const handleRoleUpdate = async (userId: number, newRole: string) => {
    try {
      setUpdatingUser(userId);
      const updated = await adminAPI.updateUserRole(userId, newRole);
      setUsers(prev => prev.map(u => u.id === userId ? updated : u));
    } catch (err) {
      console.error("Failed to update role:", err);
      alert("Failed to update user role");
    } finally {
      setUpdatingUser(null);
    }
  };

  const handleStatusUpdate = async (userId: number, newStatus: string) => {
    try {
      setUpdatingUser(userId);
      const updated = await adminAPI.updateUserStatus(userId, newStatus);
      setUsers(prev => prev.map(u => u.id === userId ? updated : u));
    } catch (err) {
      console.error("Failed to update status:", err);
      alert("Failed to update user status");
    } finally {
      setUpdatingUser(null);
    }
  };

  const handleDeleteUser = async (userId: number) => {
    if (!window.confirm("Are you sure you want to delete this user? This action cannot be undone.")) return;
    try {
      setUpdatingUser(userId);
      await adminAPI.deleteUser(userId);
      setUsers(prev => prev.filter(u => u.id !== userId));
    } catch (err) {
      console.error("Failed to delete user:", err);
      alert(err instanceof Error ? err.message : "Failed to delete user");
    } finally {
      setUpdatingUser(null);
    }
  };

  const handleWalletUpdate = (user: User) => {
    setSelectedUser(user);
    setNewBalance((user.walletBalance || 0).toString());
    setIsWalletModalOpen(true);
  };

  const confirmWalletUpdate = async () => {
    if (!selectedUser) return;
    
    const balance = parseFloat(newBalance);
    if (isNaN(balance)) {
      alert("Please enter a valid number");
      return;
    }

    try {
      setUpdatingUser(selectedUser.id);
      setIsWalletModalOpen(false);
      const updated = await adminAPI.updateUserWallet(selectedUser.id, balance);
      setUsers(prev => prev.map(u => u.id === selectedUser.id ? updated : u));
    } catch (err) {
      console.error("Failed to update wallet:", err);
      alert("Failed to update wallet balance");
    } finally {
      setUpdatingUser(null);
      setSelectedUser(null);
    }
  };

  const filteredUsers = useMemo(() => {
    return users.filter(u => {
      const matchesSearch = 
        u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (u.firstName + " " + u.lastName).toLowerCase().includes(searchQuery.toLowerCase()) ||
        u.username?.toLowerCase().includes(searchQuery.toLowerCase());
      
      const matchesRole = roleFilter === "ALL" || u.role === roleFilter;
      const matchesStatus = statusFilter === "ALL" || u.status === statusFilter;
      
      return matchesSearch && matchesRole && matchesStatus;
    });
  }, [users, searchQuery, roleFilter, statusFilter]);

  const formatDate = (d?: string) => 
    d ? new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : "—";

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-gradient-to-br from-slate-50 to-slate-100 px-4 sm:px-6 lg:px-10 pb-12 pt-20">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col md:flex-row md:items-center justify-between gap-4"
        >
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-slate-900 flex items-center gap-3">
              <Users className="w-8 h-8 text-teal-600" />
              User Management
            </h1>
            <p className="text-sm text-slate-500 mt-1">
              Manage system users, roles, and account status.
            </p>
          </div>
        </motion.div>

        {/* Filters */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
        >
          <Card className="border-slate-200/80 shadow-sm backdrop-blur-sm bg-white/80">
            <CardContent className="p-4">
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="md:col-span-2 relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    type="text"
                    placeholder="Search users by name, email or username..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500/30 focus:border-teal-400 transition-all"
                  />
                </div>
                
                <div className="relative">
                  <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                  <select
                    value={roleFilter}
                    onChange={(e) => setRoleFilter(e.target.value)}
                    className="appearance-none w-full pl-10 pr-10 py-2.5 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-teal-500/30 transition-all cursor-pointer"
                  >
                    <option value="ALL">All Roles</option>
                    <option value="CUSTOMER">Customers</option>
                    <option value="SHOP_OWNER">Shop Owners</option>
                    <option value="ADMIN">Admins</option>
                  </select>
                  <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                </div>

                <div className="relative">
                  <Shield className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                  <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    className="appearance-none w-full pl-10 pr-10 py-2.5 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-teal-500/30 transition-all cursor-pointer"
                  >
                    <option value="ALL">All Status</option>
                    <option value="ACTIVE">Active</option>
                    <option value="INACTIVE">Inactive</option>
                  </select>
                  <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                </div>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        {/* Users Table/List */}
        <div className="space-y-4">
          {isLoading ? (
            <Card className="border-slate-200">
              <CardContent className="py-20 text-center">
                <Loader2 className="w-10 h-10 text-teal-600 animate-spin mx-auto mb-4" />
                <p className="text-slate-500">Fetching users...</p>
              </CardContent>
            </Card>
          ) : error ? (
            <Card className="border-red-100 bg-red-50/30">
              <CardContent className="py-20 text-center">
                <AlertCircle className="w-12 h-12 text-red-400 mx-auto mb-4" />
                <p className="text-red-600 font-medium">{error}</p>
                <Button onClick={fetchUsers} className="mt-4" variant="outline">Try Again</Button>
              </CardContent>
            </Card>
          ) : filteredUsers.length === 0 ? (
            <Card className="border-slate-200">
              <CardContent className="py-20 text-center">
                <Users className="w-12 h-12 text-slate-200 mx-auto mb-4" />
                <p className="text-slate-400 font-medium">No users found matching your filters.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="grid grid-cols-1 gap-4">
              {filteredUsers.map((u, i) => (
                <motion.div
                  key={u.id}
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: Math.min(i * 0.05, 0.5) }}
                >
                  <Card className="relative z-10 border-slate-200/80 shadow-sm hover:shadow-md hover:z-20 transition-all group backdrop-blur-sm bg-white/90">
                    <CardContent className="p-0">
                      <div className="flex flex-col md:flex-row md:items-center gap-6 p-6">
                        {/* User Avatar/Icon */}
                        <div className="flex items-center gap-4 flex-1">
                          <div className={`w-14 h-14 rounded-2xl flex items-center justify-center shrink-0 shadow-inner ${
                            u.role === 'ADMIN' ? 'bg-purple-100 text-purple-600' :
                            u.role === 'SHOP_OWNER' ? 'bg-blue-100 text-blue-600' :
                            'bg-teal-100 text-teal-600'
                          }`}>
                            <Users className="w-7 h-7" />
                          </div>
                          <div className="min-w-0">
                            <h3 className="text-lg font-bold text-slate-900 truncate">
                              {u.firstName} {u.lastName}
                              {currentUser?.id === u.id && (
                                <span className="ml-2 px-2 py-0.5 bg-slate-100 text-slate-500 text-[10px] rounded-full uppercase tracking-wider">You</span>
                              )}
                            </h3>
                            <div className="flex flex-wrap items-center gap-x-4 gap-y-1 mt-1 text-sm text-slate-500">
                              <span className="flex items-center gap-1.5">
                                <Mail className="w-3.5 h-3.5" />
                                {u.email}
                              </span>
                              {u.phoneNumber && (
                                <span className="flex items-center gap-1.5">
                                  <Phone className="w-3.5 h-3.5" />
                                  {u.phoneNumber}
                                </span>
                              )}
                            </div>
                          </div>
                        </div>

                        {/* Badges & Stats */}
                        <div className="flex flex-wrap items-center gap-3 md:px-6 md:border-x border-slate-100">
                          <div className="flex flex-col">
                            <span className="text-[10px] uppercase tracking-wider text-slate-400 font-bold mb-1">Role</span>
                            <span className={`px-2.5 py-1 rounded-lg text-xs font-bold ${
                              u.role === 'ADMIN' ? 'bg-purple-50 text-purple-700 border border-purple-100' :
                              u.role === 'SHOP_OWNER' ? 'bg-blue-50 text-blue-700 border border-blue-100' :
                              'bg-teal-50 text-teal-700 border border-teal-100'
                            }`}>
                              {u.role?.replace('_', ' ')}
                            </span>
                          </div>
                          <div className="flex flex-col">
                            <span className="text-[10px] uppercase tracking-wider text-slate-400 font-bold mb-1">Status</span>
                            <span className={`px-2.5 py-1 rounded-lg text-xs font-bold ${
                              u.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700 border border-emerald-100' :
                              'bg-red-50 text-red-700 border border-red-100'
                            }`}>
                              {u.status || 'ACTIVE'}
                            </span>
                          </div>
                          <div className="flex flex-col hidden sm:flex">
                            <span className="text-[10px] uppercase tracking-wider text-slate-400 font-bold mb-1">Joined</span>
                            <span className="text-sm text-slate-600 flex items-center gap-1">
                              <Calendar className="w-3.5 h-3.5" />
                              {formatDate(u.createdAt as any)}
                            </span>
                          </div>
                          <div className="flex flex-col">
                            <span className="text-[10px] uppercase tracking-wider text-slate-400 font-bold mb-1">Wallet</span>
                            <button 
                              onClick={() => handleWalletUpdate(u)}
                              className="text-sm font-bold text-teal-600 hover:text-teal-700 hover:bg-teal-50 px-2 py-0.5 rounded transition-colors flex items-center gap-1.5 group/wallet"
                              title="Click to edit wallet"
                            >
                              <Wallet className="w-3.5 h-3.5" />
                              ₱{(u.walletBalance || 0).toLocaleString('en-PH', { minimumFractionDigits: 2 })}
                            </button>
                          </div>
                        </div>

                        {/* Actions */}
                        <div className="flex items-center gap-2">
                          <div className="group/actions relative">
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-9 px-3 text-slate-500 hover:text-teal-600 hover:bg-teal-50 rounded-lg transition-all"
                              disabled={updatingUser === u.id}
                            >
                              {updatingUser === u.id ? <Loader2 className="w-4 h-4 animate-spin" /> : <Shield className="w-4 h-4" />}
                              <span className="ml-2 text-xs font-semibold">Roles</span>
                              <ChevronDown className="ml-1 w-3.5 h-3.5" />
                            </Button>
                            
                            <div className="absolute right-0 top-full mt-1 w-40 bg-white border border-slate-100 shadow-xl rounded-xl p-1.5 opacity-0 invisible group-hover/actions:opacity-100 group-hover/actions:visible transition-all z-20 scale-95 group-hover/actions:scale-100 origin-top-right">
                              {['CUSTOMER', 'SHOP_OWNER', 'ADMIN'].map(role => (
                                <button
                                  key={role}
                                  onClick={() => handleRoleUpdate(u.id, role)}
                                  className={`w-full text-left px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
                                    u.role === role ? 'bg-teal-50 text-teal-600' : 'text-slate-600 hover:bg-slate-50'
                                  }`}
                                >
                                  {role.replace('_', ' ')}
                                </button>
                              ))}
                            </div>
                          </div>

                          <Button
                            variant="ghost"
                            size="sm"
                            className={`h-9 px-3 rounded-lg transition-all ${
                              u.status === 'ACTIVE' 
                                ? 'text-amber-600 hover:bg-amber-50' 
                                : 'text-emerald-600 hover:bg-emerald-50'
                            }`}
                            onClick={() => handleStatusUpdate(u.id, u.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')}
                            disabled={updatingUser === u.id}
                          >
                            {u.status === 'ACTIVE' ? <UserX className="w-4 h-4" /> : <UserCheck className="w-4 h-4" />}
                          </Button>

                          <Button
                            variant="ghost"
                            size="sm"
                            className="h-9 px-3 text-red-500 hover:bg-red-50 rounded-lg transition-all"
                            onClick={() => handleDeleteUser(u.id)}
                            disabled={updatingUser === u.id || currentUser?.id === u.id}
                          >
                            <Trash2 className="w-4 h-4" />
                          </Button>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </motion.div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Wallet Update Modal */}
      <AnimatePresence>
        {isWalletModalOpen && selectedUser && (
          <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setIsWalletModalOpen(false)}
              className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm"
            />
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 10 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 10 }}
              className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl overflow-hidden"
            >
              <div className="px-6 py-5 border-b border-slate-100 flex items-center gap-3">
                <div className="p-2 bg-teal-50 rounded-xl">
                  <Wallet className="w-5 h-5 text-teal-600" />
                </div>
                <div>
                  <h3 className="text-lg font-bold text-slate-900">Update Wallet</h3>
                  <p className="text-xs text-slate-500">Edit balance for {selectedUser.firstName} {selectedUser.lastName}</p>
                </div>
              </div>

              <div className="p-6 space-y-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">New Balance (₱)</label>
                  <div className="relative">
                    <span className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 font-medium">₱</span>
                    <input 
                      type="number"
                      value={newBalance}
                      onChange={(e) => setNewBalance(e.target.value)}
                      className="w-full pl-8 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 outline-none transition-all font-bold text-lg"
                      placeholder="0.00"
                      autoFocus
                    />
                  </div>
                </div>
                
                <div className="flex items-center gap-3 pt-2">
                  <Button 
                    variant="outline" 
                    className="flex-1"
                    onClick={() => setIsWalletModalOpen(false)}
                  >
                    Cancel
                  </Button>
                  <Button 
                    className="flex-1 bg-teal-600 hover:bg-teal-700 text-white shadow-lg shadow-teal-600/20"
                    onClick={confirmWalletUpdate}
                  >
                    Update Balance
                  </Button>
                </div>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
