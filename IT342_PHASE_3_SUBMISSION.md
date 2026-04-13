# IT342 Phase 3 – Third-Party Authentication (Google OAuth2) Submission

## 1. GitHub Link
**Repository:** https://github.com/ginmurin/IT342-Sevilla-WashMate

**Main Branch:** All code merged and pushed

**Key Commits:**
- `19e5038 - IT342 Phase 3 – Web Main Feature Completed`
- Multiple commits showing OAuth implementation progression

---

## 2. Main Feature: Google OAuth2 Authentication

The main feature implemented is **Google OAuth2 Authentication** that allows users to register and log in using their Google accounts alongside traditional email/password authentication. The system seamlessly links Google accounts with existing user profiles and automatically initializes subscriptions for new OAuth users.

---

## 3. Screenshots

### Login Page with "Sign in with Google" Button
![Login Page](./docs/screenshots/login_page_google_button.png)
- Email/password login form
- Prominent "Sign in with Google" button with Google logo
- Styled with teal color scheme matching WashMate theme
- Responsive design for desktop and mobile

### Google Account Selection
![Google Account Selection](./docs/screenshots/google_account_selection.png)
- Google's account chooser showing multiple Gmail accounts
- User can select which account to use
- Shows account email and profile information
- Demonstrates proper OAuth flow initiation

### Authentication Callback Loading State
![Auth Callback Loading](./docs/screenshots/auth_callback_loading.png)
- AuthCallback page with loading spinner
- WashMate logo animated with spinner
- Shows redirect is in progress
- URL contains access token and user data parameters

### Successful Dashboard Redirect
![Dashboard Logged In](./docs/screenshots/dashboard_logged_in.png)
- User successfully logged into customer dashboard
- User profile shows correct Gmail email address
- User greeting with first and last name
- Navigation menu and subscription status visible
- URL shows /customer (authenticated route)

### User Profile Dropdown
![User Profile Dropdown](./docs/screenshots/user_profile_dropdown.png)
- User profile button clicked
- Dropdown shows email from Google account
- First and last name extracted from Google profile
- "Profile Settings" and "Logout" options visible

### Database Records - Users Table
![Users Table OAuth Records](./docs/screenshots/db_users_oauth.png)
- Shows 3+ user records created via Google OAuth
- Columns visible: user_id, email, oauth_id, oauth_provider, email_verified, created_at
- oauth_provider = "GOOGLE" for all rows
- oauth_id column populated with Google's user sub claim
- email_verified = true (Google emails pre-verified)
- password_hash = NULL (no password for OAuth users)

### Database Records - Multiple OAuth Users
![Multiple OAuth Users](./docs/screenshots/db_multiple_oauth_users.png)
- Shows users created with different Google accounts
- Demonstrates each Gmail gets unique record
- Different oauth_id values for different users
- All have oauth_provider = "GOOGLE"
- Different email addresses for account separation

### Database Records - User Subscriptions
![User Subscriptions Auto-Init](./docs/screenshots/db_subscriptions_oauth.png)
- UserSubscription records auto-created for OAuth users
- Linked to correct user_ids
- FREE plan subscription status = ACTIVE
- Demonstrates subscription auto-initialization

### Browser Network Tab - OAuth Flow
![Network Tab OAuth Flow](./docs/screenshots/browser_network_oauth_flow.png)
- GET /api/auth/google/login (302 redirect to Google)
- accounts.google.com requests (Google OAuth servers)
- GET /api/auth/google/callback?code=... (302 redirect back)
- /auth/callback request (frontend loads)
- All requests show 200/302 status codes (success)

### Browser Console - No Errors
![Browser Console Clean](./docs/screenshots/browser_console_clean.png)
- Browser console shows no errors after login
- No CORS warnings
- Successful authentication logged
- Clean state during OAuth flow

### LocalStorage - JWT Tokens
![LocalStorage Tokens](./docs/screenshots/localstorage_tokens.png)
- washmate_access_token visible in localStorage
- washmate_refresh_token visible in localStorage
- Both tokens populated with JWT values
- Demonstrates client-side token persistence

### JWT Token Decoded
![JWT Decoded](./docs/screenshots/jwt_decoded.png)
- JWT token decoded on jwt.io
- Header: alg=HS256
- Payload shows: sub (user ID), email (from Google), role (CUSTOMER), iat, exp
- Signature validation successful
- 15-minute expiration for access token

### Frontend Code - AuthController OAuth Methods
![AuthController OAuth](./docs/screenshots/code_authcontroller_oauth.png)
- googleLogin() method: initiates OAuth flow
- googleCallback() method: handles OAuth callback
- exchangeCodeForTokens() method: code-to-token exchange
- getUserInfoFromGoogle() method: fetches user info
- All methods with proper annotations and error handling

### Frontend Code - GoogleOAuthService
![GoogleOAuthService](./docs/screenshots/code_googleoauthservice.png)
- processGoogleOAuth() method: create/link user logic
- User lookup by oauth_id
- Email-based user linking
- New user creation with oauth_id and oauth_provider
- initializeUserSubscription() for auto-subscription
- GoogleUserInfo DTO definition

---

## 4. Feature Description

### Overview
Google OAuth2 Authentication is a third-party authentication provider that enables users to:
- Sign up with a Google account
- Log in with a Google account
- Link existing email accounts to Google
- Access system features with secure JWT tokens

### Key Features

#### 4.1 Complete OAuth2 Flow
- **Authorization:** User clicks "Sign in with Google" button
- **Authentication:** Google verifies credentials and shows consent screen
- **Code Exchange:** Backend exchanges authorization code for access token
- **User Info:** Backend fetches user details from Google userinfo endpoint
- **Account Creation/Linking:** User account created or linked based on email
- **Token Generation:** Backend issues JWT tokens for API authentication
- **Redirect:** Frontend redirected to dashboard with authenticated session

#### 4.2 Account Linking
- New Gmail addresses create new user accounts
- Existing emails get linked to Google OAuth
- oauth_id stores Google's unique user identifier
- oauth_provider field set to "GOOGLE"
- Email verification inherited from Google trust

#### 4.3 JWT Token Management
- **Access Token:** 15-minute expiration for API calls
- **Refresh Token:** 7-day expiration for token refresh
- **HS256 Algorithm:** HMAC-SHA256 signature using backend secret
- **Secure Storage:** Tokens stored in localStorage
- **Auto-Refresh:** 401 responses trigger automatic token refresh

#### 4.4 Subscription Auto-Initialization
- New OAuth users automatically assigned FREE plan subscription
- Subscription status set to ACTIVE immediately
- Expiry date calculated (1 month from creation)
- Users can upgrade to BASIC, PREMIUM, or VIP plans

#### 4.5 Security Measures
- CSRF protection via state parameter
- JWT signature validation with backend secret
- Secure token exchange via HTTPS
- No hardcoded Google credentials in code
- Rate limiting on login attempts
- OAuth user emails pre-verified by Google

---

## 5. Inputs and Validations

### OAuth Login Page
**User Input:**
- Click "Sign in with Google" button

**Validations:**
- Google account selection required
- Grant permission to application
- Valid Google credentials

### Backend OAuth Processing
**Input from Google:**
- Authorization code
- State parameter (CSRF token)
- User info: email, sub (ID), name, given_name, family_name, picture

**Validations:**
- State parameter matches stored value
- Authorization code is valid
- Access token successfully obtained
- User info endpoint returns valid response
- Email is valid and not malformed
- Google sub (oauth_id) is unique or matches existing user

### JWT Token Claims
**Required Claims:**
- `sub`: User ID (numeric)
- `email`: User email address
- `role`: User role (CUSTOMER, SHOP_OWNER, ADMIN)
- `iat`: Issued at (timestamp)
- `exp`: Expiration (timestamp)

**Validations:**
- Token signature valid (HS256)
- Expiration time not elapsed
- Subject (user ID) exists in database
- Token not blacklisted

---

## 6. How the Feature Works

### Step-by-Step Workflow

#### OAuth Sign-In Flow:
1. User navigates to `/login` page
2. User clicks "Sign in with Google" button
3. **Frontend:** Redirects to `http://localhost:8080/api/auth/google/login`
4. **Backend:** Generates state parameter for CSRF protection
5. **Backend:** Redirects to Google OAuth authorization endpoint
6. **Google:** Shows account chooser and permission consent screen
7. User selects Gmail account and grants permission
8. **Google:** Redirects to `http://localhost:8080/api/auth/google/callback?code=...&state=...`
9. **Backend:** Validates state parameter
10. **Backend:** Exchanges code for Google access token via token endpoint
11. **Backend:** Calls Google userinfo endpoint with access token
12. **Backend:** Receives user info (email, name, sub)
13. **Backend:** Checks if user exists by oauth_id
    - **If yes:** Update last login timestamp
    - **If no:** Check if user exists by email
      - **If yes:** Link Google OAuth to existing account
      - **If no:** Create new user account
14. **Backend:** Sets email_verified = true (Google trust)
15. **Backend:** Creates/updates UserSubscription with FREE plan (if new user)
16. **Backend:** Generates JWT access token (15 min expiry)
17. **Backend:** Generates JWT refresh token (7 day expiry)
18. **Backend:** Redirects to `http://localhost:5173/auth/callback?accessToken=...&refreshToken=...&email=...&userId=...`
19. **Frontend:** AuthCallback page extracts parameters from URL
20. **Frontend:** Stores tokens in localStorage
21. **Frontend:** Calls login() context to set user state
22. **Frontend:** Redirects to `/customer` dashboard
23. User successfully logged in with access to all dashboard features

#### Key Decision Point - User Lookup by oauth_id:
The critical bug fix in the implementation was using the correct field for Google's unique identifier:
- **Wrong:** `response.get("id")` → returns null
- **Correct:** `response.get("sub")` → Google's standard OAuth user ID field

This ensures each Google account is properly identified and linked to the correct user.

---

## 7. API Endpoints

### OAuth Endpoints
```
GET /api/auth/google/login
  Description: Initiates Google OAuth flow
  Parameters: none
  Response: 302 redirect to Google's authorization endpoint

GET /api/auth/google/callback
  Description: Handles OAuth callback from Google
  Parameters: code (authorization code), state (CSRF token)
  Response: 302 redirect to frontend with JWT tokens
  Example: http://localhost:5173/auth/callback?accessToken=...&refreshToken=...&email=...

POST /api/auth/refresh
  Description: Refresh expired access token
  Input: { refreshToken: string }
  Output: { accessToken: string, expiresIn: number }
```

### Protected Endpoints (Require JWT)
All endpoints require `Authorization: Bearer <accessToken>` header
```
GET /api/orders - Get user's orders
GET /api/subscriptions/me - Get user's current subscription
GET /api/wallet - Get user's wallet balance
POST /api/orders/create - Create new order
POST /api/subscriptions/upgrade/{planType} - Upgrade subscription
```

---

## 8. Database Tables Involved

### users
```sql
user_id (PK)
email (UNIQUE)
oauth_id (UNIQUE, nullable) -- Google's user ID (sub claim)
oauth_provider (nullable)   -- "GOOGLE" or "EMAIL_PASSWORD"
email_verified (boolean)    -- true for OAuth users
password_hash (nullable)    -- NULL for OAuth users
first_name
last_name
username
phone_number
role (CUSTOMER | SHOP_OWNER | ADMIN)
created_at
updated_at
```

### user_subscriptions
```sql
user_subscription_id (PK)
user_id (FK)
subscription_id (FK)
status (ACTIVE | EXPIRED)
start_date
expiry_date
created_at
```

### subscriptions
```sql
subscription_id (PK)
plan_type (FREE | BASIC | PREMIUM | VIP)
plan_price
features_json
active (boolean)
```

---

## 9. Frontend Components Added/Modified

### New Files
- `web/src/app/pages/AuthCallback.tsx` - OAuth callback handler
- `web/src/app/config/GoogleOAuthConfig.java` - OAuth configuration class (Backend)
- `web/src/app/config/JwtConfig.java` - JWT configuration class (Backend)

### Modified Components
- `web/src/app/pages/Login.tsx` - Added "Sign in with Google" button
- `web/src/app/contexts/AuthContext.tsx` - JWT token management
- `web/src/app/components/ProtectedRoute.tsx` - JWT token validation
- `web/src/app/utils/api.ts` - HTTP interceptors for JWT
- `washmate/src/main/java/.../config/SecurityConfig.java` - Custom JWT decoder
- `washmate/src/main/java/.../service/GoogleOAuthService.java` - Google OAuth logic
- `washmate/src/main/java/.../controller/AuthController.java` - OAuth endpoints

---

## 10. Technology Stack

### Frontend
- React 18 with TypeScript
- React Router for navigation and OAuth callback handling
- LocalStorage for JWT token persistence
- Axios with HTTP interceptors for token management
- Tailwind CSS for styling
- Lucide React for icons

### Backend
- Spring Boot 3.x
- Spring Security with OAuth2 Client
- Custom JwtDecoder for HS256 validation
- JJWT library for JWT token generation
- RestTemplate for Google API calls
- PostgreSQL database
- Hibernate/JPA for persistence
- SLF4J for logging

### Third-Party Services
- Google OAuth 2.0 servers for authentication
- Google userinfo endpoint for user data

---

## 11. Testing Performed

### Manual Testing
- ✅ OAuth flow with new Google account
- ✅ OAuth flow with existing email account linking
- ✅ Multiple different Gmail accounts create separate users
- ✅ Token storage in localStorage
- ✅ Protected endpoints accessible with JWT
- ✅ Token expiration and refresh mechanism
- ✅ Auto-subscription initialization
- ✅ Browser console shows no errors
- ✅ Network tab shows proper OAuth flow
- ✅ JWT tokens properly formatted and decodable

### Validations Tested
- ✅ OAuth state parameter validation
- ✅ Google user info extraction (sub field fix)
- ✅ Email uniqueness constraint
- ✅ OAuth ID uniqueness constraint
- ✅ JWT signature validation
- ✅ Token expiration enforcement
- ✅ User role-based access control
- ✅ CORS handling for cross-port requests

### Error Scenarios Tested
- ✅ Invalid/expired OAuth state
- ✅ Invalid authorization code
- ✅ Malformed JWT token
- ✅ Expired access token (refresh triggered)
- ✅ Invalid refresh token
- ✅ Duplicate email linking prevention

---

## 12. Key Challenges & Solutions

### Challenge 1: Wrong Google User ID Field
**Problem:** Code was using `response.get("id")` which returns null in Google's userinfo endpoint
**Solution:** Changed to `response.get("sub")` which is the correct field for OAuth user ID
**Impact:** Each Google account now properly linked to correct user

### Challenge 2: JWT Validation Mismatch
**Problem:** SecurityConfig using Supabase issuer-uri but backend generates own JWTs
**Solution:** Created custom JwtDecoder bean using backend's JWT secret (HS256)
**Impact:** Backend-issued JWTs properly validated on all endpoints

### Challenge 3: User Lookup Issues
**Problem:** Controllers using findByOauthId(jwt.getSubject()) but JWT subject contains userId, not oauth_id
**Solution:** Changed to parse subject as Long and use findById(userId)
**Impact:** All protected endpoints correctly identify authenticated users

### Challenge 4: Frontend/Backend Port Mismatch
**Problem:** Frontend (5173) and backend (8080) on different ports, relative OAuth URL failed
**Solution:** Used absolute URL with environment variable for API base URL
**Impact:** OAuth redirect works correctly across ports

### Challenge 5: React Re-render Warnings
**Problem:** useEffect dependency array causing infinite re-renders on AuthCallback
**Solution:** Changed to empty dependency array (OAuth callback runs once on mount)
**Impact:** No more React warnings, smooth authentication

---

## 13. Summary

This implementation delivers a **production-ready Google OAuth2 authentication system** that:
- Provides seamless third-party sign-in alongside email/password auth
- Automatically creates accounts for new Google users
- Links existing email accounts to Google OAuth
- Uses secure JWT tokens for API authentication  
- Auto-initializes FREE subscriptions for new OAuth users
- Maintains data integrity with proper oauth_id validation
- Handles errors gracefully with user-friendly messaging
- Follows Spring Security OAuth2 best practices
- Properly integrates with existing WashMate system

The system is fully functional from login button through authenticated dashboard access, with proper database records, token management, and error handling throughout.


