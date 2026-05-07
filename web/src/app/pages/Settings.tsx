import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import { authAPI } from "../utils/api";
import { getCurrentSubscription, type UserSubscriptionData } from "../services/subscription";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { Input } from "../components/Input";
import {
  ArrowLeft,
  Eye,
  EyeOff,
  Check,
  ShieldCheck,
  Copy,
  Mail,
  User as UserIcon,
  Phone,
  CheckCircle,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";

export default function Settings() {
  const navigate = useNavigate();
  const { user, setUser } = useAuth();
  const [activeTab, setActiveTab] = useState<"account" | "security">("account");
  const [isEditing, setIsEditing] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(user?.twoFactorEnabled || false);
  const [enabling2FA, setEnabling2FA] = useState(false);
  const [verifying2FA, setVerifying2FA] = useState(false);
  const [twoFACode, setTwoFACode] = useState("");
  const [twoFACodeSent, setTwoFACodeSent] = useState(false);
  const [show2FADisableConfirm, setShow2FADisableConfirm] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // Account form state
  const [formData, setFormData] = useState({
    firstName: user?.firstName || "",
    lastName: user?.lastName || "",
    email: user?.email || "",
    phone: user?.phone || user?.phoneNumber || "",
  });

  // Security form state
  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const getErrorMessage = (error: unknown, fallback: string) =>
    error instanceof Error ? error.message : fallback;

  useEffect(() => {
    if (!user || isEditing) return;
    let cancelled = false;

    const loadAccountInfo = async () => {
      try {
        const currentUser = await authAPI.me();
        if (cancelled) return;

        setUser(currentUser);
        setFormData({
          firstName: currentUser.firstName || "",
          lastName: currentUser.lastName || "",
          email: currentUser.email || "",
          phone: currentUser.phone || currentUser.phoneNumber || "",
        });
        setTwoFactorEnabled(!!currentUser.twoFactorEnabled);
      } catch (error) {
        console.error("Failed to load account info:", error);
      }
    };

    loadAccountInfo();
    return () => {
      cancelled = true;
    };
  }, [user?.id, isEditing, setUser]);



  const formatDate = (value?: string | null) => {
    if (!value) return "—";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "—";
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };



  const handleAccountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPasswordData({
      ...passwordData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSaveAccount = async () => {
    setLoading(true);
    try {
      const trimmedPhone = formData.phone.trim();
      const updatedUser = await authAPI.updateMe({
        firstName: formData.firstName,
        lastName: formData.lastName,
        ...(trimmedPhone ? { phoneNumber: trimmedPhone } : {}),
      });

      setUser(updatedUser);
      setFormData({
        firstName: updatedUser.firstName || "",
        lastName: updatedUser.lastName || "",
        email: updatedUser.email || "",
        phone: updatedUser.phone || updatedUser.phoneNumber || "",
      });

      setSuccessMessage("Account updated successfully");
      setTimeout(() => setSuccessMessage(""), 3000);
      setIsEditing(false);
    } catch (error) {
      console.error("Error updating account:", error);
      setErrorMessage(getErrorMessage(error, "Error updating account"));
      setTimeout(() => setErrorMessage(""), 3000);
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async () => {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setErrorMessage("Passwords do not match");
      setTimeout(() => setErrorMessage(""), 3000);
      return;
    }
    setLoading(true);
    try {
      await authAPI.changePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
      });
      setSuccessMessage("Password changed successfully");
      setTimeout(() => setSuccessMessage(""), 3000);
      setPasswordData({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } catch (error) {
      console.error("Error changing password:", error);
      setErrorMessage(getErrorMessage(error, "Failed to change password"));
      setTimeout(() => setErrorMessage(""), 3000);
    } finally {
      setLoading(false);
    }
  };

  const handleEnable2FA = async () => {
    setEnabling2FA(true);
    try {
      await authAPI.sendTwoFactorCode();
      setTwoFACodeSent(true);
      setSuccessMessage("Verification code sent to your email");
      setTimeout(() => setSuccessMessage(""), 3000);
    } catch (error) {
      console.error("Error sending 2FA code:", error);
      setErrorMessage(getErrorMessage(error, "Failed to send verification code"));
      setTimeout(() => setErrorMessage(""), 3000);
    } finally {
      setEnabling2FA(false);
    }
  };

  const handleVerify2FA = async () => {
    if (!twoFACode || twoFACode.length !== 6) {
      setErrorMessage("Please enter a valid 6-digit code");
      setTimeout(() => setErrorMessage(""), 3000);
      return;
    }
    setVerifying2FA(true);
    try {
      const result = await authAPI.enableTwoFactor(twoFACode);
      const enabled = result.twoFactorEnabled ?? true;

      setTwoFactorEnabled(enabled);
      if (user) {
        setUser({ ...user, twoFactorEnabled: enabled });
      }
      setTwoFACodeSent(false);
      setTwoFACode("");
      setSuccessMessage("Two-factor authentication enabled successfully");
      setTimeout(() => setSuccessMessage(""), 3000);
    } catch (error) {
      console.error("Error verifying 2FA code:", error);
      setErrorMessage(getErrorMessage(error, "Failed to enable 2FA"));
      setTimeout(() => setErrorMessage(""), 3000);
    } finally {
      setVerifying2FA(false);
    }
  };

  const handleDisable2FA = async () => {
    setLoading(true);
    try {
      const result = await authAPI.disableTwoFactor();
      const enabled = result.twoFactorEnabled ?? false;

      setTwoFactorEnabled(enabled);
      if (user) {
        setUser({ ...user, twoFactorEnabled: enabled });
      }
      setShow2FADisableConfirm(false);
      setSuccessMessage("Two-factor authentication disabled");
      setTimeout(() => setSuccessMessage(""), 3000);
    } catch (error) {
      console.error("Error disabling 2FA:", error);
      setErrorMessage(getErrorMessage(error, "Failed to disable 2FA"));
      setTimeout(() => setErrorMessage(""), 3000);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex-1 min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-20 pb-12">
      <div className="max-w-4xl mx-auto px-4 space-y-8">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex items-center gap-4"
        >
          <button
            onClick={() => {
              if (user?.role === "SHOP_OWNER") navigate("/shop");
              else if (user?.role === "ADMIN") navigate("/admin");
              else navigate("/customer");
            }}
            className="p-2 hover:bg-white rounded-lg transition-colors"
          >
            <ArrowLeft className="w-5 h-5 text-slate-600" />
          </button>
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Settings</h1>
            <p className="text-slate-500 mt-1">Manage your account and preferences</p>
          </div>
        </motion.div>

        {/* Success Message */}
        {successMessage && (
          <motion.div
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg flex items-center gap-2 text-emerald-700"
          >
            <Check className="w-5 h-5" />
            {successMessage}
          </motion.div>
        )}

        {/* Tabs */}
        <div className="flex gap-8 border-b border-slate-200">
          <button
            onClick={() => setActiveTab("account")}
            className={`pb-3 font-medium transition-colors border-b-2 ${
              activeTab === "account"
                ? "border-teal-600 text-teal-600"
                : "border-transparent text-slate-600 hover:text-slate-900"
            }`}
          >
            Account
          </button>
          <button
            onClick={() => setActiveTab("security")}
            className={`pb-3 font-medium transition-colors border-b-2 ${
              activeTab === "security"
                ? "border-teal-600 text-teal-600"
                : "border-transparent text-slate-600 hover:text-slate-900"
            }`}
          >
            Security
          </button>

        </div>

        {/* Account Tab */}
        {activeTab === "account" && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.1 }}
          >
            {/* Glassmorphism Card */}
            <div className="backdrop-blur-md bg-white/60 border border-white/40 rounded-2xl shadow-lg overflow-hidden">
              {/* Header */}
              <div className="px-8 py-6 border-b border-white/20 bg-white/40">
                <h2 className="text-xl font-semibold text-slate-900">
                  Account Information
                </h2>
              </div>

              {/* Content */}
              <div className="px-8 py-8">
                {!isEditing ? (
                  <div className="space-y-8">
                    {/* First Name & Last Name Grid */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                      {/* First Name */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <UserIcon className="w-4 h-4 text-teal-500" />
                          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
                            First Name
                          </p>
                        </div>
                        <p className="text-lg font-medium text-slate-900">
                          {formData.firstName}
                        </p>
                      </div>

                      {/* Last Name */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <UserIcon className="w-4 h-4 text-blue-500" />
                          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
                            Last Name
                          </p>
                        </div>
                        <p className="text-lg font-medium text-slate-900">
                          {formData.lastName}
                        </p>
                      </div>
                    </div>

                    {/* Email */}
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <Mail className="w-4 h-4 text-purple-500" />
                        <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
                          Email Address
                        </p>
                        <span className="text-xs font-semibold text-emerald-600 bg-emerald-50 px-2 py-1 rounded">
                          Verified
                        </span>
                      </div>
                      <p className="text-lg font-medium text-slate-900">
                        {formData.email}
                      </p>
                    </div>

                    {/* Phone */}
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <Phone className="w-4 h-4 text-indigo-500" />
                        <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
                          Phone Number
                        </p>
                      </div>
                      <p className="text-lg font-medium text-slate-900">
                        {formData.phone || <span className="text-slate-400">Not added</span>}
                      </p>
                    </div>

                    {/* Edit Button */}
                    <Button
                      onClick={() => setIsEditing(true)}
                      className="bg-gradient-to-r from-teal-500 to-blue-600 hover:from-teal-600 hover:to-blue-700 text-white font-medium shadow-lg shadow-teal-500/20"
                    >
                      Edit Account
                    </Button>
                  </div>
                ) : (
                  <div className="space-y-6">
                    {/* Edit Mode - First Name & Last Name */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div>
                        <label className="block text-sm font-medium text-slate-700 mb-2 flex items-center gap-2">
                          <UserIcon className="w-4 h-4 text-teal-500" />
                          First Name
                        </label>
                        <Input
                          type="text"
                          name="firstName"
                          value={formData.firstName}
                          onChange={handleAccountChange}
                          placeholder="First name"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-slate-700 mb-2 flex items-center gap-2">
                          <UserIcon className="w-4 h-4 text-blue-500" />
                          Last Name
                        </label>
                        <Input
                          type="text"
                          name="lastName"
                          value={formData.lastName}
                          onChange={handleAccountChange}
                          placeholder="Last name"
                        />
                      </div>
                    </div>

                    {/* Email */}
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2 flex items-center gap-2">
                        <Mail className="w-4 h-4 text-purple-500" />
                        Email (Read-only)
                      </label>
                      <Input
                        type="email"
                        value={formData.email}
                        disabled
                        className="bg-slate-100 text-slate-600"
                      />
                    </div>

                    {/* Phone */}
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2 flex items-center gap-2">
                        <Phone className="w-4 h-4 text-indigo-500" />
                        Phone Number
                      </label>
                      <Input
                        type="tel"
                        name="phone"
                        value={formData.phone}
                        onChange={handleAccountChange}
                        placeholder="+63"
                      />
                    </div>

                    {/* Action Buttons */}
                    <div className="flex gap-3 pt-4">
                      <Button
                        onClick={handleSaveAccount}
                        loading={loading}
                        className="bg-gradient-to-r from-teal-500 to-blue-600 hover:from-teal-600 hover:to-blue-700 text-white font-medium shadow-lg shadow-teal-500/20 flex-1"
                      >
                        Save Changes
                      </Button>
                      <Button
                        onClick={() => setIsEditing(false)}
                        variant="outline"
                        className="flex-1"
                      >
                        Cancel
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </motion.div>
        )}

        {/* Security Tab */}
        {activeTab === "security" && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.1 }}
            className="space-y-8"
          >
            {/* Password Change Card */}
            <Card className="border-slate-200 shadow-sm">
              <CardHeader className="bg-gradient-to-r from-blue-50 to-blue-50 border-b border-slate-200">
                <CardTitle className="text-slate-900">Change Password</CardTitle>
              </CardHeader>
              <CardContent className="space-y-6 pt-8">
                <p className="text-sm text-slate-600">
                  Ensure your account is using a long, random password to stay secure.
                </p>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                      Current Password
                    </label>
                    <div className="relative">
                      <Input
                        type={showPassword ? "text" : "password"}
                        name="currentPassword"
                        value={passwordData.currentPassword}
                        onChange={handlePasswordChange}
                        placeholder="Enter current password"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                      New Password
                    </label>
                    <div className="relative">
                      <Input
                        type={showPassword ? "text" : "password"}
                        name="newPassword"
                        value={passwordData.newPassword}
                        onChange={handlePasswordChange}
                        placeholder="Enter new password"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                      Confirm Password
                    </label>
                    <div className="relative">
                      <Input
                        type={showPassword ? "text" : "password"}
                        name="confirmPassword"
                        value={passwordData.confirmPassword}
                        onChange={handlePasswordChange}
                        placeholder="Confirm new password"
                      />
                      <button
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-2.5 text-slate-600 hover:text-slate-900"
                      >
                        {showPassword ? (
                          <EyeOff className="w-5 h-5" />
                        ) : (
                          <Eye className="w-5 h-5" />
                        )}
                      </button>
                    </div>
                  </div>
                </div>
                <Button
                  onClick={handleChangePassword}
                  loading={loading}
                  className="bg-blue-600 hover:bg-blue-700 text-white w-full"
                >
                  Update Password
                </Button>
              </CardContent>
            </Card>

            {/* Two-Factor Authentication Card */}
            <Card className="border-slate-200 shadow-sm">
              <CardHeader className="bg-gradient-to-r from-purple-50 to-purple-50 border-b border-slate-200">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <ShieldCheck className="w-5 h-5 text-purple-600" />
                    <CardTitle className="text-slate-900">Two-Factor Authentication</CardTitle>
                  </div>
                  <div className="flex items-center gap-2">
                    <div
                      className={`w-3 h-3 rounded-full ${
                        twoFactorEnabled ? "bg-emerald-500" : "bg-slate-300"
                      }`}
                    />
                    <span
                      className={`text-sm font-medium ${
                        twoFactorEnabled ? "text-emerald-600" : "text-slate-600"
                      }`}
                    >
                      {twoFactorEnabled ? "Enabled" : "Disabled"}
                    </span>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-6 pt-8">
                <p className="text-sm text-slate-600">
                  Add an extra layer of security to your account by requiring a verification code in addition to your password.
                </p>

                <AnimatePresence mode="wait">
                  {!twoFactorEnabled ? (
                    <motion.div
                      key="disabled"
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                      exit={{ opacity: 0 }}
                      className="space-y-4"
                    >
                      {!twoFACodeSent ? (
                        <Button
                          onClick={handleEnable2FA}
                          loading={enabling2FA}
                          className="bg-purple-600 hover:bg-purple-700 text-white w-full"
                        >
                          <ShieldCheck className="w-4 h-4 mr-2" />
                          Enable Two-Factor Authentication
                        </Button>
                      ) : (
                        <motion.div
                          initial={{ opacity: 0, y: 8 }}
                          animate={{ opacity: 1, y: 0 }}
                          className="space-y-4 p-4 bg-purple-50 rounded-lg border border-purple-200"
                        >
                          <div className="flex items-start gap-3">
                            <Mail className="w-5 h-5 text-purple-600 mt-0.5 flex-shrink-0" />
                            <div>
                              <p className="text-sm font-medium text-slate-900">
                                Verification code sent
                              </p>
                              <p className="text-xs text-slate-600 mt-1">
                                Check your email for a 6-digit verification code
                              </p>
                            </div>
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-slate-700 mb-2">
                              Enter Verification Code
                            </label>
                            <Input
                              type="text"
                              maxLength={6}
                              value={twoFACode}
                              onChange={(e) => setTwoFACode(e.target.value.replace(/\D/g, ""))}
                              placeholder="000000"
                              className="text-center text-2xl tracking-widest font-mono"
                            />
                          </div>
                          <div className="flex gap-3">
                            <Button
                              onClick={handleVerify2FA}
                              loading={verifying2FA}
                              className="bg-purple-600 hover:bg-purple-700 text-white flex-1"
                            >
                              Verify & Enable
                            </Button>
                            <Button
                              onClick={() => {
                                setTwoFACodeSent(false);
                                setTwoFACode("");
                              }}
                              variant="outline"
                              className="flex-1"
                            >
                              Cancel
                            </Button>
                          </div>
                        </motion.div>
                      )}
                    </motion.div>
                  ) : (
                    <motion.div
                      key="enabled"
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                      exit={{ opacity: 0 }}
                      className="space-y-4"
                    >
                      <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg flex items-start gap-3">
                        <Check className="w-5 h-5 text-emerald-600 mt-0.5 flex-shrink-0" />
                        <div>
                          <p className="text-sm font-medium text-emerald-900">
                            Two-factor authentication is active
                          </p>
                          <p className="text-xs text-emerald-700 mt-1">
                            You'll need to enter a verification code when logging in
                          </p>
                        </div>
                      </div>
                      <Button
                        onClick={() => setShow2FADisableConfirm(true)}
                        loading={loading}
                        variant="outline"
                        className="w-full text-red-600 hover:bg-red-50 border-red-200"
                      >
                        Disable Two-Factor Authentication
                      </Button>
                    </motion.div>
                  )}
                </AnimatePresence>
              </CardContent>
            </Card>
          </motion.div>
        )}



        {/* Error/Success Messages */}
        <AnimatePresence>
          {successMessage && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 10 }}
              className="fixed top-4 right-4 bg-green-500 text-white px-4 py-3 rounded-lg shadow-lg flex items-center gap-2"
            >
              <Check className="w-5 h-5" />
              {successMessage}
            </motion.div>
          )}
          {errorMessage && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 10 }}
              className="fixed top-4 right-4 bg-red-500 text-white px-4 py-3 rounded-lg shadow-lg"
            >
              {errorMessage}
            </motion.div>
          )}
        </AnimatePresence>

        {/* 2FA Disable Confirmation Modal */}
        <AnimatePresence>
          {show2FADisableConfirm && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
            >
              <motion.div
                initial={{ scale: 0.9, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.9, opacity: 0 }}
                className="bg-white rounded-lg shadow-xl p-6 max-w-sm w-full mx-4"
              >
                <h3 className="text-lg font-semibold text-slate-900 mb-2">
                  Disable Two-Factor Authentication?
                </h3>
                <p className="text-slate-600 text-sm mb-6">
                  This will remove the extra layer of security from your account. You'll only need your password to log in.
                </p>
                <div className="flex gap-3">
                  <button
                    onClick={() => setShow2FADisableConfirm(false)}
                    className="flex-1 px-4 py-2 text-slate-700 border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors font-medium"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleDisable2FA}
                    disabled={loading}
                    className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 transition-colors font-medium"
                  >
                    {loading ? "Disabling..." : "Disable"}
                  </button>
                </div>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
