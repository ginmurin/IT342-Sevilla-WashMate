# IT342 Phase 3 – Google OAuth2 Implementation Report

**GitHub Repository:** https://github.com/ginmurin/IT342-Sevilla-WashMate

**Main Branch:** main


**WashMate – Third-Party Authentication Integration (Google OAuth2)**

---

## 2. Third-Party Authentication Provider Used
**Google OAuth 2.0**

- **Provider:** Google Identity Platform (accounts.google.com)
- **Authentication Standard:** OAuth 2.0 (Authorization Code Flow)
- **User ID Field:** `sub` claim (Google's unique identifier)
- **Trust Model:** Google email verification (email_verified automatically set to true)

---

## 3. Implementation Summary

### Overview
Google OAuth2 authentication was integrated into WashMate to provide seamless third-party sign-in, allowing users to:
- Register and log in using Google accounts
- Automatically link existing email accounts to Google OAuth
- Access system features with secure JWT tokens
- Auto-initialize FREE subscription for new OAuth users

### How It Works
1. **Authentication Initiation:** User clicks "Sign in with Google" button on login page
2. **Authorization Request:** Frontend redirects to `/api/auth/google/login` which initiates Google OAuth flow
3. **User Consent:** Google shows account selector and permission consent screen
4. **Authorization Code:** Google redirects back with authorization code
5. **Token Exchange:** Backend exchanges code for Google access token
6. **User Info Retrieval:** Backend calls Google userinfo endpoint to fetch user details
7. **Account Creation/Linking:** 
   - If user exists by oauth_id → update login timestamp
   - If new Gmail → create new user account
   - If email exists → link Google OAuth to existing account
8. **JWT Generation:** Backend generates access token (15 min) and refresh token (7 day)
9. **Frontend Redirect:** User redirected to dashboard with tokens in localStorage
10. **Protected Routes:** All subsequent API calls use JWT authentication

### Key Decision Points
- **User Lookup:** Used `sub` claim (not `id`) for unique Google user identification
- **Account Linking:** Linked by email if oauth_id doesn't match existing user
- **Subscription Init:** Auto-create FREE subscription for new OAuth users
- **Email Verification:** Trusted Google's email verification (email_verified = true)

---

## 4. Backend Technologies/Libraries Used

### Framework & Security
- **Spring Boot 3.x** – Application framework
- **Spring Security 6.x** – Security framework with OAuth2 support
- **Spring Web** – REST API implementation
- **Spring Data JPA** – Data persistence layer

### JWT Token Management
- **JJWT (JSON Web Token)** – JWT token generation and validation
- **HS256 Algorithm** – HMAC-SHA256 signature
- **Custom JwtDecoder** – Bean for validating backend-issued JWTs
- **Token Provider Utility** – Token generation and claim extraction

### OAuth2 & Google Integration
- **Spring Security OAuth2 Client** – OAuth2 client implementation
- **RestTemplate** – HTTP client for Google API calls
- **Google OAuth 2.0 Endpoints:**
  - Authorization: `https://accounts.google.com/o/oauth2/v2/auth`
  - Token: `https://oauth2.googleapis.com/token`
  - UserInfo: `https://openidconnect.googleapis.com/v1/userinfo`

### API Endpoints Created
```java
// OAuth Flow
GET /api/auth/google/login          // Initiates OAuth2 flow
GET /api/auth/google/callback       // Handles OAuth callback

// Token Management
POST /api/auth/refresh              // Refresh expired access token

// Protected Endpoints (require JWT)
GET /api/orders
POST /api/orders/create
GET /api/subscriptions/me
POST /api/subscriptions/upgrade/{planType}
GET /api/wallet
POST /api/wallet/topup/initiate
```

### Database Changes
- **users table:** Added `oauth_id` (UNIQUE), `oauth_provider`, removed password requirement for OAuth users
- **user_subscriptions:** Auto-created on new OAuth user registration
- All OAuth users get email_verified = true

---

## 5. Frontend Integration Summary

### UI Components Modified
- **Login.tsx** – Added "Sign in with Google" button with Google logo
- **AuthCallback.tsx** – New component to handle OAuth redirect and token extraction
- **AuthContext.tsx** – Updated with JWT token management (access + refresh tokens)
- **ProtectedRoute.tsx** – Enhanced with JWT validation and auto-refresh
- **api.ts** – Added HTTP interceptors for JWT attachment and auto-refresh

### Authentication Flow (Frontend)
```typescript
// 1. User clicks "Sign in with Google"
onClick={() => window.location.href = '/api/auth/google/login'}

// 2. AuthCallback.tsx receives tokens and user info
// Extracts from URL: accessToken, refreshToken, email, userId

// 3. Store tokens in localStorage
localStorage.setItem('washmate_access_token', accessToken)
localStorage.setItem('washmate_refresh_token', refreshToken)

// 4. Update AuthContext
setUser({ email, userId, role })
setIsAuthenticated(true)

// 5. Redirect to dashboard
navigate('/customer')

// 6. HTTP Interceptor attaches JWT to all requests
headers.Authorization = `Bearer ${accessToken}`

// 7. 401 triggers auto-refresh (using refresh token)
if (error.status === 401) {
  // Call POST /api/auth/refresh with refreshToken
  // Update accessToken
  // Retry original request
}
```

### Styling & UX
- **Color Scheme:** Teal/Blue (matching WashMate branding)
- **Button Design:** Google logo + "Sign in with Google" text
- **Loading State:** AuthCallback shows spinner while processing
- **Error Handling:** User-friendly error messages on OAuth failure
- **Responsive:** Works on desktop and mobile devices

---

## 6. Challenges Encountered & Solutions

### Challenge 1: Wrong Google User ID Field
**Problem:** Code used `response.get("id")` which returns null from Google userinfo endpoint

**Root Cause:** Google OAuth specifies `sub` (subject) claim as the unique user identifier in the userinfo endpoint

**Solution:** Changed to `response.get("sub")`

**Impact:** Each Google account now properly linked to correct user DB record

**Code Location:** `GoogleOAuthService.java:processGoogleOAuth()`

---

### Challenge 2: JWT Validation Mismatch
**Problem:** SecurityConfig was configured with Supabase issuer-uri, but backend generates its own JWTs

**Error:** Invalid signature on backend-issued JWT tokens

**Root Cause:** Multiple JWT issuers creating validation conflicts

**Solution:** 
- Created custom `JwtDecoder` bean that uses backend's JWT secret
- Configured SecurityConfig to use custom decoder instead of Supabase issuer-uri
- Used HS256 signing algorithm with consistent secret

**Impact:** All protected endpoints correctly validate JWTs

**Code Location:** `JwtConfig.java`, `SecurityConfig.java`

---

### Challenge 3: User Lookup Issues in Controllers
**Problem:** Controllers attempted `findByOauthId(jwt.getSubject())` but JWT subject contains userId (Long), not oauth_id

**Error:** NullPointerException or 401 Unauthorized on protected endpoints

**Root Cause:** Confusing JWT claim semantics - `sub` in JWT is the user ID, not oauth_id

**Solution:** Parse JWT subject as Long and use `userRepository.findById(userId)`

**Impact:** All protected endpoints (orders, wallet, subscriptions) now correctly identify authenticated users

**Code Location:** `OrderController.java`, `SubscriptionController.java`, `WalletController.java`

---

### Challenge 4: Frontend/Backend Port Mismatch
**Problem:** Frontend (localhost:5173) and backend (localhost:8080) on different ports; relative URL in OAuth login failed

**Error:** 404 redirects when clicking "Sign in with Google"

**Root Cause:** Relative URLs don't work for cross-origin redirect

**Solution:** 
- Used absolute URL `window.location.href = 'http://localhost:8080/api/auth/google/login'`
- Configurable via environment variable for production

**Impact:** OAuth redirect works correctly across ports

**Code Location:** `Login.tsx`

---

### Challenge 5: React Re-render Loop on AuthCallback
**Problem:** useEffect dependency array causing infinite re-renders

**Error:** React warnings in console, callback page stuck in loading

**Root Cause:** Missing/incorrect dependency array on useEffect

**Solution:** Changed to empty dependency array `[]` (OAuth callback should run once on component mount)

**Impact:** AuthCallback page loads cleanly with proper token handling

**Code Location:** `AuthCallback.tsx`

---

### Challenge 6: LocalStorage vs SessionStorage Decision
**Problem:** Deciding where to persist JWT tokens

**Tradeoff:** 
- localStorage: Persists across browser close (better UX but security risk if XSS)
- sessionStorage: Lost on close (secure but worse UX)

**Solution:** Used localStorage with secure HTTPS in production and XSS protection best practices

**Impact:** Users stay logged in across sessions while maintaining reasonable security

---

## 7. Proof of Implementation

### Screenshot Evidence

#### 1. Sign-In Button
![Login Page with Google Button](./docs/screenshots/login_page_google_button.png)
- Login form with email/password fields
- Prominent "Sign in with Google" button with official Google logo
- Clean, modern design matching WashMate theme

#### 2. Google Account Selection
![Google Account Selection](./docs/screenshots/google_account_selection.png)
- Google's native account chooser showing multiple Gmail accounts
- User selects which account to sign in with
- Demonstrates proper OAuth 2.0 flow beginning

#### 3. OAuth Callback Loading
![Auth Callback Loading State](./docs/screenshots/auth_callback_loading.png)
- AuthCallback component showing loading spinner
- WashMate logo with animated spinner
- Processing OAuth redirect and token extraction

#### 4. Successful Dashboard Access
![Dashboard After OAuth Login](./docs/screenshots/dashboard_logged_in.png)
- User successfully logged into customer dashboard
- Shows correct Google email in profile
- Access to all authenticated features
- Confirms JWT tokens working correctly

#### 5. User Profile Display
![User Profile Dropdown](./docs/screenshots/user_profile_dropdown.png)
- User profile button showing Google email
- First and last name extracted from Google profile
- Dropdown menu with Profile and Logout options

#### 6. Database Records - Users Table
![Users Table with OAuth Records](./docs/screenshots/db_users_oauth.png)
- Multiple user records created via Google OAuth
- Columns: user_id, email, oauth_id, oauth_provider, email_verified
- oauth_provider = "GOOGLE" for OAuth users
- oauth_id populated with Google's unique identifier
- password_hash = NULL (no password for OAuth users)
- email_verified = true (inherited from Google)

#### 7. Browser Network Tab - OAuth Flow
![Network Tab OAuth Requests](./docs/screenshots/browser_network_oauth_flow.png)
- GET /api/auth/google/login (302 redirect to Google)
- Requests to accounts.google.com (Google OAuth servers)
- GET /api/auth/google/callback with authorization code
- All requests returning 200/302 status codes

#### 8. Browser Console - Clean
![Console with No Errors](./docs/screenshots/browser_console_clean.png)
- No errors or CORS warnings after OAuth login
- Clean console during authentication flow
- Successful token operations logged

#### 9. LocalStorage - JWT Tokens
![LocalStorage Tokens Visible](./docs/screenshots/localstorage_tokens.png)
- washmate_access_token: JWT token with 15-minute expiration
- washmate_refresh_token: JWT token with 7-day expiration
- Both tokens properly formatted and populated

#### 10. JWT Decoded
![JWT Token Decoded](./docs/screenshots/jwt_decoded.png)
- Header: alg=HS256, typ=JWT
- Payload: sub (user ID), email, role (CUSTOMER), iat, exp
- Signature: valid HS256 signature using backend secret
- Token structure correct and claims populated

#### 11. Code - AuthController OAuth Methods
![AuthController.java OAuth Methods](./docs/screenshots/code_authcontroller_oauth.png)
- googleLogin() method initiating OAuth flow
- googleCallback() method handling OAuth redirect
- exchangeCodeForTokens() method doing code-to-token exchange
- Proper error handling throughout

#### 12. Code - GoogleOAuthService
![GoogleOAuthService.java Implementation](./docs/screenshots/code_googleoauthservice.png)
- processGoogleOAuth() with account creation/linking logic
- Lookup by oauth_id and email
- Auto-subscription initialization
- Google user info extraction with correct fields

---

## 8. Commit History Evidence

### OAuth Implementation Commit (Today)

**Commit:** [`6912d15`](https://github.com/ginmurin/IT342-Sevilla-WashMate/commit/6912d1560f78a8b5c7f17ba9ce9e0f6356b515c4)

**Message:** Update IT342 Phase 3 submission with Google OAuth2 implementation details

**Changes in this commit:**
- Updated IT342_PHASE_3_SUBMISSION.md with comprehensive OAuth documentation
- Added 40 files related to OAuth implementation
- Database schema updates (oauth_id, oauth_provider fields)
- Backend configuration and services
- Frontend authentication components and utilities

### Key Implementation Files (in commits)

```
✅ Backend Configuration
  - GoogleOAuthConfig.java (OAuth2 client setup)
  - JwtConfig.java (JWT decoder bean)
  - SecurityConfig.java (Spring Security OAuth2 integration)

✅ Backend Services
  - GoogleOAuthService.java (OAuth logic + account linking)
  - AuthService.java (modified for OAuth)
  - JwtTokenProvider.java (JWT token generation)

✅ Backend DTOs
  - GoogleIdTokenRequest.java (OAuth ID token validation)
  - AuthResponse.java (modified with JWT tokens)

✅ Frontend Pages
  - Login.tsx (added Google button)
  - AuthCallback.tsx (new - OAuth redirect handler)

✅ Frontend Context & Utilities
  - AuthContext.tsx (JWT token management)
  - ProtectedRoute.tsx (JWT validation)
  - api.ts (HTTP interceptors for JWT)

✅ Database
  - users table: oauth_id, oauth_provider columns
  - user_subscriptions: auto-create for new OAuth users
```

### Verification Commands
```bash
# View full OAuth implementation commits
git log --oneline | grep -i "oauth"

# See all changes in OAuth feature branch
git log main --oneline --all | head -20

# View specific OAuth commit details
git show 2fdd9c2  # Google OAuth commit

# Check file changes across OAuth commits
git log --name-status --oneline | grep -A 20 "oauth"
```

---

## 9. Summary

### What Was Implemented
✅ **Complete Google OAuth2 Flow** – From login button to authenticated dashboard access

✅ **Account Linking** – New Gmail creates account; existing email gets linked to Google

✅ **JWT Token Management** – 15-min access tokens + 7-day refresh tokens with HS256 signing

✅ **Auto-Subscription** – New OAuth users automatically get FREE plan subscription

✅ **Protected Endpoints** – All API routes secured with JWT validation and auto-refresh

✅ **Error Handling** – Graceful failures with user-friendly messages

✅ **Database Integrity** – Unique constraints on oauth_id, proper email_verified tracking

### Technologies Delivered
- **Backend:** Spring Boot 3.x + Spring Security + JJWT
- **Frontend:** React 18 + TypeScript + JWT interceptors
- **OAuth Provider:** Google Identity Platform (OAuth 2.0)
- **Database:** PostgreSQL with oauth_id and oauth_provider fields

### Key Challenges Resolved
1. ✅ Correct Google user ID field (`sub` not `id`)
2. ✅ JWT validation with backend secret (HS256)
3. ✅ User lookup logic (JWT subject is userId, not oauth_id)
4. ✅ Cross-port OAuth redirect (absolute URLs)
5. ✅ React re-render optimization
6. ✅ Token persistence strategy

### Result
**Fully functional third-party authentication system** enabling users to:
- Sign in with Google in one click
- Seamlessly link existing accounts to Google
- Access all WashMate features with secure JWT tokens
- Maintain session across page reloads
- Automatically start with FREE subscription tier

The implementation is production-ready, properly documented, and fully integrated with the existing WashMate platform.

