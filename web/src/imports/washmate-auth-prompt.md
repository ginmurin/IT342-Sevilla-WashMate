WASHMATE - FRONTEND DEVELOPMENT PROMPT
========================================

PROJECT OVERVIEW:
Build a complete authentication system and role-based dashboards for WashMate, a laundry service management platform. The design must be clean, modern, and aligned with a professional laundry service aesthetic.

TECH STACK:
- React 18+ with TypeScript
- Tailwind CSS for styling
- React Router for navigation
- Axios for API calls
- JWT token management
- Form validation (React Hook Form + Zod)

COLOR PALETTE (Laundry Service Theme):
- Primary Blue: #2563EB (trust, cleanliness)
- Secondary Teal: #14B8A6 (freshness)
- Success Green: #10B981 (clean/complete)
- Warning Orange: #F59E0B (attention)
- Error Red: #EF4444 (alerts)
- White: #FFFFFF (cleanliness)
- Light Gray: #F3F4F6 (background)
- Dark Gray: #1F2937 (text)

DESIGN PRINCIPLES:
- Clean, minimal interface (laundry = cleanliness)
- Smooth animations and transitions
- Mobile-first responsive design
- Consistent spacing (4px grid)
- Professional business aesthetic
- Accessibility (WCAG AA compliant)

===========================================
PART 1: AUTHENTICATION PAGES
===========================================

1. LOGIN PAGE
-----------------------------------
URL: /login

LAYOUT:
- Two-column layout (50/50 split on desktop, stacked on mobile)
- Left column: Branding
- Right column: Login form

LEFT COLUMN (BRANDING):
- Full-height background: Gradient from #2563EB to #14B8A6
- WashMate logo (centered, white)
- Tagline: "Your Laundry, Simplified" (white, below logo)
- Decorative elements: Washing machine icon, soap bubbles (subtle)
- Responsive: Hidden on mobile, full screen on desktop

RIGHT COLUMN (LOGIN FORM):
- Card container (white background, subtle shadow)
- Form elements:
  * Page title: "Welcome Back" (large, bold)
  * Subtitle: "Sign in to manage your laundry services"
  
  * Email input:
    - Label: "Email Address"
    - Placeholder: "juan@example.com"
    - Icon: Envelope icon (left side)
    - Validation: Real-time email format check
    - Error state: Red border + error message
  
  * Password input:
    - Label: "Password"
    - Placeholder: "Enter your password"
    - Icon: Lock icon (left side)
    - Toggle visibility: Eye icon (right side)
    - Validation: Required, min 8 characters
  
  * Remember me checkbox (optional)
  
  * "Forgot Password?" link (right-aligned, blue text)
  
  * Login button:
    - Full width
    - Blue background (#2563EB)
    - White text
    - Rounded corners
    - Hover: Darken slightly
    - Loading state: Spinner inside button
    - Text: "Sign In"
  
  * Divider: "OR" (centered, gray line)
  
  * Google OAuth button:
    - Full width
    - White background, gray border
    - Google logo (left)
    - Text: "Continue with Google"
    - Hover: Light gray background
  
  * Register link: "Don't have an account? Sign up" (center, bottom)

FUNCTIONALITY:
- Form validation on submit
- Display error messages below each field
- API call to POST /api/auth/login
- Store JWT token in localStorage
- Redirect based on role:
  * CUSTOMER → /customer/dashboard
  * SHOP_OWNER → /shop/dashboard
  * ADMIN → /admin/dashboard
- Show loading spinner during authentication
- Handle errors gracefully (invalid credentials, server error)

RESPONSIVE:
- Mobile: Single column, full screen form
- Tablet: Stacked layout
- Desktop: Two-column split


2. REGISTER PAGE
-----------------------------------
URL: /register

LAYOUT:
Same two-column layout as login

LEFT COLUMN: Same branding as login

RIGHT COLUMN (REGISTER FORM):
- Card container
- Form elements:
  * Page title: "Get Started"
  * Subtitle: "Create your WashMate account"
  
  * Full Name input:
    - Label: "Full Name"
    - Placeholder: "Juan Dela Cruz"
    - Icon: User icon
    - Validation: Required, min 2 words
  
  * Email input:
    - Same as login
    - Additional validation: Check if email exists (on blur)
  
  * Phone Number input:
    - Label: "Phone Number"
    - Placeholder: "+63 912 345 6789"
    - Icon: Phone icon
    - Validation: PH format (+63)
  
  * Password input:
    - Label: "Password"
    - Placeholder: "Create a strong password"
    - Icon: Lock icon
    - Password strength indicator:
      * Weak (red), Medium (orange), Strong (green)
      * Show requirements: min 8 chars, uppercase, number
  
  * Confirm Password input:
    - Label: "Confirm Password"
    - Validation: Must match password
    - Show checkmark when matched
  
  * Terms & Conditions checkbox:
    - Required
    - Text: "I agree to the Terms of Service and Privacy Policy"
    - Links clickable
  
  * Register button:
    - Full width, blue
    - Text: "Create Account"
    - Loading state
  
  * Divider: "OR"
  
  * Google OAuth button:
    - Same as login
    - Text: "Sign up with Google"
  
  * Login link: "Already have an account? Sign in"

FUNCTIONALITY:
- Real-time validation on all fields
- Password strength checker
- Email availability check (debounced)
- API call to POST /api/auth/register
- On success: Show email verification message
- Send verification email
- Redirect to /verify-email page

EMAIL VERIFICATION SUCCESS PAGE:
- Simple centered card
- Success icon (green checkmark)
- Title: "Verify Your Email"
- Message: "We've sent a verification link to [email]"
- "Resend Email" button (with cooldown timer)
- "Change Email" link


3. FORGOT PASSWORD PAGE (Optional)
-----------------------------------
- Simple centered form
- Email input
- Submit button
- Success: "Reset link sent to your email"


===========================================
PART 2: ROLE-BASED DASHBOARDS
===========================================

SHARED DASHBOARD LAYOUT:
- Top navigation bar (fixed)
- Sidebar navigation (collapsible on mobile)
- Main content area
- Responsive hamburger menu

TOP NAVIGATION BAR (All Roles):
Component: Navbar
Height: 64px
Background: White
Border bottom: Light gray

Elements (left to right):
- Logo: WashMate logo + text (left, clickable → home)
- Search bar (center, optional for ADMIN/SHOP_OWNER)
- Notification bell icon (badge count)
- User profile dropdown (right):
  * Avatar image
  * User name
  * Role badge
  * Dropdown menu:
    - Profile Settings
    - Help & Support
    - Logout


DASHBOARD 1: CUSTOMER DASHBOARD
-----------------------------------
URL: /customer/dashboard

SIDEBAR NAVIGATION:
- Dashboard (active)
- My Orders
- Subscription
- Saved Addresses
- Feedback
- Settings

MAIN CONTENT:
Hero Section:
- Welcome message: "Welcome back, [User Name]!"
- Quick action buttons:
  * "Book Laundry Service" (large, blue, prominent)
  * "Track Orders" (outline)

Stats Cards (3 columns):
Card 1: Active Orders
- Icon: Washing machine
- Number: "2" (large, blue)
- Label: "Active Orders"
- Link: "View all →"

Card 2: Subscription Status
- Icon: Crown
- Text: "Weekly Plan" or "No Active Plan"
- Link: "Manage →"

Card 3: Wallet Balance
- Icon: Wallet
- Amount: "₱500.00" (large)
- Link: "Top up →"

Recent Orders Section:
- Heading: "Recent Orders"
- Table/Cards showing last 5 orders:
  * Order number
  * Service type icon
  * Status badge (color-coded)
  * Date
  * Amount
  * "Track" button
- "View All Orders" link

Quick Service Selection:
- Heading: "Book a Service"
- 3 service cards (horizontal):
  * Wash & Fold (washing machine icon)
  * Wash & Iron (iron icon)
  * Dry Clean (hanger icon)
- Each card: Click to start booking

DESIGN:
- Clean, minimal cards
- Consistent spacing (24px between sections)
- Blue primary actions
- Light gray backgrounds
- Icons from lucide-react or heroicons


DASHBOARD 2: SHOP OWNER DASHBOARD
-----------------------------------
URL: /shop/dashboard

SIDEBAR NAVIGATION:
- Dashboard (active)
- Today's Orders
- All Orders
- Schedule
- Shop Settings
- Performance
- Logout

MAIN CONTENT:
Shop Header:
- Shop name (large, bold)
- Shop status badge: "Active" (green)
- Quick actions: "Update Hours" | "View Profile"

Performance Cards (4 columns):
Card 1: Today's Orders
- Icon: Package
- Number: "12" (large, blue)
- Subtext: "+3 from yesterday"

Card 2: In Progress
- Icon: Loader
- Number: "5" (orange)
- Link: "View →"

Card 3: Completed Today
- Icon: Check
- Number: "3" (green)

Card 4: Revenue Today
- Icon: Money
- Amount: "₱3,250" (large, green)
- Subtext: "+12% from avg"

Pending Orders Section:
- Heading: "Pending Orders" (with badge count)
- Order cards (vertical list):
  Each card:
  * Order number (large, prominent)
  * Customer name + phone (with call icon)
  * Service type + quantity
  * Pickup address (truncated)
  * Pickup time (highlighted if soon)
  * Status badge
  * "Update Status" dropdown button (blue)
  * "View Details" link

Chart Section (Optional):
- Weekly orders chart (line/bar)
- Service distribution (pie chart)

DESIGN:
- Operational efficiency focus
- Large, easy-to-read numbers
- Color-coded status indicators
- Quick action buttons prominent
- Orange/yellow for urgent items


DASHBOARD 3: ADMIN DASHBOARD
-----------------------------------
URL: /admin/dashboard

SIDEBAR NAVIGATION:
- Dashboard (active)
- User Management
- Shop Management
- Service Configuration
- All Orders
- Revenue Analytics
- Feedback Management
- System Settings

MAIN CONTENT:
Admin Overview Header:
- "System Overview" heading
- Date range selector (Last 30 days)
- Export button

Key Metrics Grid (4 columns):
Card 1: Total Users
- Icon: Users
- Number: "1,234" (large)
- Subtext: "+56 this month" (green)
- Breakdown: "1,180 Customers, 48 Shops, 6 Admins"

Card 2: Total Revenue
- Icon: Currency
- Amount: "₱125,450" (large, green)
- Trend: "+12.5%" (green arrow)

Card 3: Active Orders
- Icon: Package
- Number: "47" (blue)
- Breakdown: "12 Pending, 18 Processing"

Card 4: System Health
- Icon: Activity
- Status: "All Systems Operational" (green)
- Link: "View details"

Charts Section (2 columns):
Left: Revenue Trends
- Line chart (last 30 days)
- Toggle: Daily | Weekly | Monthly

Right: Service Distribution
- Pie chart showing:
  * Wash & Fold: 55%
  * Wash & Iron: 30%
  * Dry Clean: 15%

Recent Activity Feed:
- Timeline of recent system events:
  * New user registered
  * Order completed
  * Feedback received
  * Payment processed
- Timestamps
- User/action details

Quick Actions Panel:
- "Add New User" button
- "Update Pricing" button
- "View Pending Feedback" button (with badge)
- "Generate Report" button

DESIGN:
- Professional business aesthetic
- Data visualization prominent
- Purple/gray color scheme
- Minimal distractions
- Information-dense but organized


===========================================
PART 3: SHARED COMPONENTS
===========================================

1. LOADING SPINNER
- Blue circular spinner
- Optional text: "Loading..."
- Centered on page or inline

2. ERROR MESSAGE
- Red background (light)
- Red border
- Error icon (X in circle)
- Error text
- Dismissible

3. SUCCESS MESSAGE
- Green background (light)
- Green border
- Checkmark icon
- Success text
- Auto-dismiss after 3s

4. EMPTY STATE
- Illustration or icon
- Message: "No data found"
- Call-to-action button (if applicable)

5. CONFIRMATION MODAL
- Overlay (dark, semi-transparent)
- Centered card
- Title
- Message
- Two buttons: "Cancel" (gray) | "Confirm" (blue/red)

6. NOTIFICATION DROPDOWN
- Dropdown from bell icon
- List of notifications:
  * Icon
  * Title
  * Message (truncated)
  * Time ago
  * Read/unread indicator
- "View All" link at bottom
- "Mark all as read" option


===========================================
PART 4: TECHNICAL REQUIREMENTS
===========================================

ROUTING:
```javascript
// Public routes
/login
/register
/verify-email
/forgot-password

// Protected routes (require auth)
/customer/dashboard (CUSTOMER role only)
/shop/dashboard (SHOP_OWNER role only)
/admin/dashboard (ADMIN role only)

// Redirect logic:
- Logged in user on /login → redirect to their dashboard
- Non-authenticated user on protected route → redirect to /login
- Wrong role on route → redirect to their dashboard
```

AUTHENTICATION:
```javascript
// Store in localStorage:
{
  token: "jwt_token_here",
  user: {
    id: 1,
    name: "Juan Dela Cruz",
    email: "juan@example.com",
    role: "CUSTOMER"
  }
}

// Add to all API requests:
headers: {
  'Authorization': `Bearer ${token}`
}

// On 401 response: Clear storage, redirect to /login
```

API ENDPOINTS:
```javascript
// Auth
POST /api/auth/register
POST /api/auth/login
POST /api/auth/google
POST /api/auth/logout
POST /api/auth/verify-email

// User
GET /api/user/profile
GET /api/user/dashboard (role-specific data)

// Protected by role
GET /api/customer/orders
GET /api/shop/orders
GET /api/admin/users
```

FORM VALIDATION:
- Use Zod schema validation
- Real-time validation on blur
- Display errors below fields
- Disable submit until valid

RESPONSIVE BREAKPOINTS:
- Mobile: < 640px
- Tablet: 640px - 1024px
- Desktop: > 1024px

ANIMATIONS:
- Page transitions: Fade in
- Button hover: Scale 1.02
- Card hover: Lift shadow
- Loading: Smooth spinner rotation
- Keep subtle and professional


===========================================
PART 5: FILE STRUCTURE
===========================================

```
src/
├── components/
│   ├── auth/
│   │   ├── LoginForm.tsx
│   │   ├── RegisterForm.tsx
│   │   └── OAuthButton.tsx
│   ├── dashboard/
│   │   ├── CustomerDashboard.tsx
│   │   ├── ShopOwnerDashboard.tsx
│   │   └── AdminDashboard.tsx
│   ├── layout/
│   │   ├── Navbar.tsx
│   │   ├── Sidebar.tsx
│   │   └── DashboardLayout.tsx
│   ├── shared/
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   ├── Card.tsx
│   │   ├── Modal.tsx
│   │   ├── Loading.tsx
│   │   └── ErrorMessage.tsx
├── pages/
│   ├── Login.tsx
│   ├── Register.tsx
│   ├── VerifyEmail.tsx
│   ├── CustomerDashboard.tsx
│   ├── ShopDashboard.tsx
│   └── AdminDashboard.tsx
├── hooks/
│   ├── useAuth.ts
│   ├── useUser.ts
│   └── useApi.ts
├── utils/
│   ├── auth.ts
│   ├── api.ts
│   └── validation.ts
├── types/
│   └── index.ts
└── App.tsx
```


===========================================
PART 6: DELIVERABLES
===========================================

1. Login Page (fully functional)
2. Register Page (with validation)
3. Email Verification Page
4. Customer Dashboard (with mock data)
5. Shop Owner Dashboard (with mock data)
6. Admin Dashboard (with mock data)
7. Shared components library
8. Protected routing setup
9. JWT authentication flow
10. Responsive design (mobile/tablet/desktop)


===========================================
TESTING CHECKLIST
===========================================

Authentication:
- [ ] Can register new user
- [ ] Can login with email/password
- [ ] Can login with Google OAuth
- [ ] JWT token stored correctly
- [ ] Redirects based on role work
- [ ] Protected routes block non-authenticated users
- [ ] Logout clears token and redirects

Role-Based Access:
- [ ] CUSTOMER sees customer dashboard
- [ ] SHOP_OWNER sees shop dashboard
- [ ] ADMIN sees admin dashboard
- [ ] Wrong role redirects correctly

Validation:
- [ ] Email format validated
- [ ] Password strength shown
- [ ] Required fields enforced
- [ ] Error messages display correctly

Responsive:
- [ ] Works on mobile (< 640px)
- [ ] Works on tablet (640-1024px)
- [ ] Works on desktop (> 1024px)
- [ ] Navigation adapts to screen size

Performance:
- [ ] Fast page loads
- [ ] Smooth animations
- [ ] No layout shifts
- [ ] Optimized images


This is a complete, production-ready authentication and dashboard system with clean laundry service design! 🧺✨