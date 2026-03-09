# WashMate - Laundry Service Management Platform

A complete authentication system and role-based dashboards for a laundry service management platform built with React, TypeScript, Tailwind CSS, and React Router.

## Features

### Authentication
- ✅ Login with email/password
- ✅ Registration with validation
- ✅ Email verification flow
- ✅ Password strength indicator
- ✅ Forgot password functionality
- ✅ JWT token management
- ✅ Google OAuth UI (mock)

### Role-Based Dashboards
- ✅ **Customer Dashboard** - Book services, track orders, manage subscriptions
- ✅ **Shop Owner Dashboard** - Manage orders, track revenue, view schedule
- ✅ **Admin Dashboard** - System overview, analytics, user management

### Design
- Clean, modern UI with laundry service theme
- Responsive design (mobile, tablet, desktop)
- Blue (#2563EB) and Teal (#14B8A6) color scheme
- Smooth animations and transitions
- Accessible components

## Test Credentials

Use these credentials to test different user roles:

### Customer Account
- **Email:** customer@washmate.com
- **Password:** password123

### Shop Owner Account
- **Email:** shop@washmate.com
- **Password:** password123

### Admin Account
- **Email:** admin@washmate.com
- **Password:** password123

## Tech Stack

- **React 18** with TypeScript
- **React Router** for navigation
- **Tailwind CSS** for styling
- **React Hook Form** + **Zod** for validation
- **Axios** for API calls
- **Lucide React** for icons
- **Recharts** for data visualization

## Project Structure

```
src/app/
├── components/
│   ├── auth/           # Login & Register forms
│   ├── layout/         # Navbar, Sidebar, Dashboard layout
│   ├── shared/         # Reusable components (Loading, Error, Success)
│   └── ProtectedRoute.tsx
├── pages/              # Route pages
├── types/              # TypeScript interfaces
├── utils/              # Helper functions (auth, API, validation)
└── routes.tsx          # Route configuration
```

## Key Components

### Authentication Flow
1. User logs in with credentials
2. JWT token stored in localStorage
3. Redirected to role-specific dashboard
4. Protected routes enforce authentication
5. Logout clears token and redirects

### Protected Routes
Routes are protected by role:
- `/customer/dashboard` - CUSTOMER only
- `/shop/dashboard` - SHOP_OWNER only
- `/admin/dashboard` - ADMIN only

Wrong role access redirects to correct dashboard.

## Design System

### Colors
- Primary Blue: `#2563EB`
- Secondary Teal: `#14B8A6`
- Success Green: `#10B981`
- Warning Orange: `#F59E0B`
- Error Red: `#EF4444`
- Background: `#F3F4F6`

### Typography
All typography uses default theme styles that can be overridden with Tailwind utility classes.

## Mock Data

The application uses mock data for demonstration:
- Mock authentication API with hardcoded users
- Sample orders and statistics
- Simulated notifications
- Example charts and analytics

## Getting Started

The application is ready to use. Simply:
1. Visit the login page
2. Use one of the test credentials above
3. Explore the role-specific dashboard

## Future Enhancements

- Real backend API integration
- Real-time order tracking
- Payment processing
- SMS notifications
- Service booking flow
- Advanced analytics
- Mobile app version
