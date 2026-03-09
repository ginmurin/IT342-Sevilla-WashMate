WASHMATE - FRONTEND DEVELOPMENT PROMPT
========================================

PROJECT OVERVIEW:
Build a complete landing page, authentication system, and role-based dashboards for WashMate, a laundry service management platform. The design must be clean, modern, and aligned with a professional laundry service aesthetic with engaging visuals and media.

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
- High-quality imagery and illustrations
- Modern photography style

===========================================
MEDIA & IMAGE SPECIFICATIONS
===========================================

IMAGE STYLE GUIDE:

PHOTOGRAPHY STYLE:
- Bright, natural lighting
- Clean, modern settings
- Real people (diverse, authentic)
- Professional but approachable
- High resolution (min 1920px width)
- Optimized for web (WebP format preferred)

COLOR GRADING:
- Slightly cool tones (blue/teal shift)
- High contrast, vibrant colors
- Clean whites and bright spaces
- Consistent with brand colors

ILLUSTRATION STYLE:
- Flat design with subtle gradients
- Blue/teal color palette
- Minimalist line art
- Modern, friendly aesthetic
- SVG format for scalability

ICON STYLE:
- Outline style (stroke-based)
- Rounded corners and edges
- Consistent stroke width (2px)
- 24x24px base size
- Blue (#2563EB) or Dark Gray (#1F2937)

STOCK PHOTO SOURCES (Recommended):
- Unsplash (free, high-quality)
- Pexels (free)
- Freepik (illustrations)
- Lucide Icons (React icons library)
- Heroicons (Tailwind UI icons)

IMAGE KEYWORDS FOR SEARCH:
- "laundry service professional"
- "clean folded clothes"
- "washing machine modern"
- "laundry basket fresh"
- "professional cleaning service"
- "happy customer laundry"
- "delivery person uniform"
- "mobile app smartphone"


REQUIRED IMAGES BY SECTION:

Landing Page:
- Hero image: Modern laundry facility or happy customer (1920x1080px)
- Service cards: 3 photos (wash/fold, iron, dry clean) (800x600px each)
- Process steps: 4 illustrations (order, pickup, clean, deliver)
- Testimonial avatars: 3-4 customer photos (circular, 120x120px)
- App mockup: Smartphone showing app interface (600x1200px)
- Background patterns: Subtle washing machine/bubble textures

Authentication Pages:
- Login/Register left column: Gradient with laundry-themed illustration
- Success state: Animated checkmark icon (Lottie JSON)
- Error state: Warning icon
- OAuth icons: Google logo (official brand assets)

Dashboards:
- Empty states: Custom illustrations (no data found)
- Service type icons: Washing machine, iron, hanger (SVG)
- Status indicators: Colored badges/dots
- Avatar placeholders: Initials with gradient background
- Charts: Use Recharts or Chart.js library

PLACEHOLDER IMAGES (Development):
- Use: https://placehold.co/ or https://picsum.photos/
- Example: https://placehold.co/1920x1080/2563EB/FFFFFF/png?text=Hero+Image


IMAGE OPTIMIZATION:
- Format: WebP with JPG fallback
- Lazy loading: Use loading="lazy" attribute
- Responsive images: Use srcset and sizes attributes
- Compression: Use TinyPNG or Squoosh
- CDN: Consider Cloudinary or imgix for production

EXAMPLE RESPONSIVE IMAGE:
```html
<picture>
  <source srcset="hero-1920.webp 1920w, hero-1280.webp 1280w" type="image/webp">
  <source srcset="hero-1920.jpg 1920w, hero-1280.jpg 1280w" type="image/jpeg">
  <img src="hero-1280.jpg" alt="Professional laundry service" loading="lazy">
</picture>
```


===========================================
PART 0: LANDING PAGE (NEW!)
===========================================

URL: / (home page)

STRUCTURE:
1. Navigation Bar (sticky)
2. Hero Section
3. Features Section
4. How It Works Section
5. Services Section
6. Testimonials Section
7. Download App Section
8. Footer

---

SECTION 1: NAVIGATION BAR
-----------------------------------
Layout: Fixed top, white background, subtle shadow

Elements (left to right):
- Logo: WashMate logo (clickable → home)
- Navigation links:
  * Features (scroll to #features)
  * How It Works (scroll to #how-it-works)
  * Services (scroll to #services)
  * Pricing (scroll to #pricing)
- CTA Buttons:
  * "Login" (outline button, blue)
  * "Get Started" (solid button, blue)

Mobile: Hamburger menu, slide-in drawer

Scroll behavior: On scroll down, add background blur + shadow


---

SECTION 2: HERO SECTION
-----------------------------------
Layout: Full viewport height, two columns (60/40 split)

LEFT COLUMN (Content):
- Eyebrow text: "LAUNDRY SERVICE MADE SIMPLE" (small, teal, uppercase)
- Headline: "Your Laundry, Delivered Fresh & Folded" (very large, bold, dark gray)
- Subheadline: "Book professional laundry services in seconds. Get your clothes picked up, cleaned, and delivered back to you." (medium, gray)
- CTA buttons:
  * "Book Now" (large, blue, primary)
  * "Watch Demo" (outline, with play icon)
- Trust indicators:
  * "⭐⭐⭐⭐⭐ 4.9/5 from 1,200+ customers"
  * Small logos: "As seen on..." (optional)

RIGHT COLUMN (Visual):
- Large hero image:
  * Option A: Happy customer holding fresh laundry basket
  * Option B: Modern laundry facility interior
  * Option C: Smartphone mockup showing app with floating UI elements
- Background: Subtle gradient (blue to teal, very light)
- Decorative elements: Floating soap bubbles (CSS animations)

Media:
- Hero image: 1200x900px, high quality
- Format: WebP with JPG fallback
- Alt text: "Professional laundry service delivery"

Animations:
- Headline: Fade in + slide up
- Buttons: Fade in with delay
- Hero image: Fade in + gentle float
- Bubbles: Continuous float animation


---

SECTION 3: FEATURES SECTION
-----------------------------------
Heading: "Why Choose WashMate?"
Subheading: "Professional laundry service at your fingertips"

Layout: 3 columns (cards)

CARD 1: Fast & Convenient
- Icon: Clock/Lightning (blue, 64px)
- Image: Person using mobile app (400x300px, rounded corners)
- Title: "Same-Day Service"
- Description: "Order before 10 AM, get your laundry back the same day"
- Badge: "24/7 Booking Available"

CARD 2: Quality Guaranteed
- Icon: Shield/Checkmark (teal, 64px)
- Image: Professionally folded clothes (400x300px)
- Title: "Premium Quality"
- Description: "Expert cleaning with eco-friendly detergents and careful handling"
- Badge: "100% Satisfaction Guaranteed"

CARD 3: Track Everything
- Icon: Location pin/Map (blue, 64px)
- Image: Phone showing order tracking (400x300px)
- Title: "Real-Time Tracking"
- Description: "Know exactly where your laundry is from pickup to delivery"
- Badge: "Live Updates"

Design:
- White cards with subtle shadow
- Hover: Lift effect + scale 1.02
- Icons: Blue circular background
- Images: Rounded corners (12px), subtle zoom on hover

Media per card:
- Feature icon: 64x64px SVG
- Feature image: 400x300px WebP
- Badge: Small pill-shaped label


---

SECTION 4: HOW IT WORKS
-----------------------------------
Background: Light gray (#F9FAFB)
Heading: "Simple as 1-2-3-4"

Layout: Horizontal process flow (4 steps)

STEP 1: Book Online
- Large number: "01" (teal, very large)
- Icon: Smartphone with app (illustration, 200x200px)
- Title: "Book Your Service"
- Description: "Choose your service type, schedule pickup time, and confirm your address"
- Image: Animated illustration of person tapping phone
- Arrow: Right-pointing arrow (connect to next step)

STEP 2: We Pick Up
- Large number: "02" (blue, very large)
- Icon: Delivery person at door (illustration, 200x200px)
- Title: "Free Pickup"
- Description: "Our professional staff collects your laundry at your chosen time"
- Image: Illustration of delivery person with laundry bag
- Arrow: Right-pointing arrow

STEP 3: We Clean
- Large number: "03" (teal, very large)
- Icon: Washing machine in motion (illustration, 200x200px)
- Title: "Expert Cleaning"
- Description: "Your clothes are professionally washed, dried, and folded with care"
- Image: Animated washing machine with bubbles
- Arrow: Right-pointing arrow

STEP 4: We Deliver
- Large number: "04" (blue, very large)
- Icon: Happy customer receiving package (illustration, 200x200px)
- Title: "Fresh & Delivered"
- Description: "Receive your clean, fresh laundry at your doorstep within 24 hours"
- Image: Illustration of happy customer
- Checkmark: Large green checkmark (completion)

Media:
- Step illustrations: 200x200px SVG or Lottie animations
- Connecting arrows: Animated dashed lines
- Background: Subtle pattern (washing machine icons, very light)

Animations:
- On scroll: Steps fade in sequentially with stagger
- Numbers: Count up animation
- Illustrations: Gentle bounce on appear
- Arrows: Animate from left to right


---

SECTION 5: SERVICES SECTION
-----------------------------------
Heading: "Our Services"
Subheading: "Professional care for every fabric"

Layout: 3 service cards (large, prominent)

SERVICE 1: Wash & Fold
- Large image: Neatly folded clothes stack (600x400px)
- Icon overlay: Washing machine icon (white, 48px)
- Title: "Wash & Fold"
- Price: "Starting at ₱35/kg"
- Features list:
  ✓ Same-day service available
  ✓ Eco-friendly detergents
  ✓ Sorted by color and fabric
  ✓ Neatly folded and packaged
- CTA: "Book Now" button (blue)

SERVICE 2: Wash & Iron
- Large image: Perfectly ironed dress shirts (600x400px)
- Icon overlay: Iron icon (white, 48px)
- Title: "Wash & Iron"
- Price: "Starting at ₱45/kg"
- Features list:
  ✓ Professional pressing
  ✓ Wrinkle-free guarantee
  ✓ Hangers included
  ✓ Same-day express available
- CTA: "Book Now" button (blue)
- Badge: "Most Popular" (orange, top-right corner)

SERVICE 3: Dry Cleaning
- Large image: Clean suit on hanger (600x400px)
- Icon overlay: Hanger icon (white, 48px)
- Title: "Dry Cleaning"
- Price: "Starting at ₱150/piece"
- Features list:
  ✓ Delicate fabric care
  ✓ Stain removal specialists
  ✓ Premium finishing
  ✓ 48-hour service
- CTA: "Book Now" button (blue)

Design:
- Cards: White background, hover effect
- Images: Full-width, gradient overlay (bottom)
- Price: Large, bold, prominent
- Features: Checkmark icons (green)

Media:
- Service photos: 600x400px each, high quality
- Icon overlays: 48x48px white SVG
- Badge graphics: Small corner ribbon


---

SECTION 6: TESTIMONIALS
-----------------------------------
Background: White
Heading: "What Our Customers Say"

Layout: Carousel/Slider (3 visible at once on desktop)

TESTIMONIAL CARD STRUCTURE:
- Star rating: ⭐⭐⭐⭐⭐ (5 stars, gold)
- Quote: "WashMate saved me so much time! The quality is amazing and the app makes everything super easy. Highly recommend!"
- Customer photo: Circular avatar (80x80px)
- Customer name: "Maria Santos"
- Customer location: "Cebu City"
- Verified badge: Small checkmark icon

Sample testimonials (3-5):
1. Busy Professional: Time-saving, convenient
2. Parent: Reliable, kid-friendly
3. Business Owner: Great for uniforms
4. Student: Affordable, fast service
5. Expat: Easy to use, trustworthy

Design:
- Card: Light gray background, rounded corners
- Quote: Large quotation marks (decorative)
- Photos: Real customer photos or diverse stock photos
- Carousel: Smooth auto-scroll with manual controls

Media:
- Customer avatars: 80x80px circular, WebP
- Star icons: 16x16px SVG (gold #F59E0B)
- Background: Subtle pattern or gradient


---

SECTION 7: DOWNLOAD APP / CTA SECTION
-----------------------------------
Background: Blue gradient (#2563EB to #14B8A6)
Layout: Two columns

LEFT COLUMN (Content):
- Heading: "Get the WashMate App" (white, large)
- Subheading: "Book services, track orders, and manage your account on the go"
- Feature highlights:
  * 📱 Easy booking in seconds
  * 📍 Real-time order tracking
  * 💳 Secure wallet payments
  * 🎁 Exclusive app-only deals
- Download buttons:
  * App Store badge (official Apple asset)
  * Google Play badge (official Google asset)
- QR code: Scan to download (optional)

RIGHT COLUMN (Visual):
- Smartphone mockup showing app interface (800x1600px)
- Multiple screen mockups (carousel of app screens)
- Floating UI elements (notifications, badges)
- Hand holding phone (optional)

Media:
- Phone mockup: High-res PNG with transparency
- App screenshots: 6-8 screens showing key features
- Store badges: Official SVG from Apple/Google
- QR code: Generated PNG (200x200px)

Animations:
- Phone mockup: Gentle float animation
- Screenshots: Auto-rotate carousel
- Badges: Scale on hover


---

SECTION 8: FOOTER
-----------------------------------
Background: Dark gray (#1F2937)
Text color: Light gray/white

Layout: 4 columns + bottom bar

COLUMN 1: Brand
- WashMate logo (white version)
- Tagline: "Your Laundry, Simplified"
- Social media icons:
  * Facebook (icon + link)
  * Instagram (icon + link)
  * Twitter (icon + link)
- Icons: 24x24px, light gray, hover: blue

COLUMN 2: Quick Links
- About Us
- How It Works
- Services
- Pricing
- Contact Us

COLUMN 3: Support
- Help Center
- FAQs
- Terms of Service
- Privacy Policy
- Refund Policy

COLUMN 4: Contact
- Email: support@washmate.ph
- Phone: +63 912 345 6789
- Address: "Cebu City, Philippines"
- Business hours: "Mon-Sun, 7 AM - 9 PM"

BOTTOM BAR:
- Copyright: "© 2024 WashMate. All rights reserved."
- Payment icons: GCash, Maya, Visa, Mastercard (grayscale, 32px)

Media:
- Logo: White SVG version
- Social icons: 24x24px outline style
- Payment logos: Official brand assets, grayscale


---

LANDING PAGE ANIMATIONS & INTERACTIONS
-----------------------------------

SCROLL ANIMATIONS (Use AOS or Framer Motion):
- Fade in: Hero headline, section headings
- Slide up: Feature cards, service cards
- Stagger: Process steps, testimonials
- Parallax: Background images (subtle)
- Number counter: Statistics (if added)

HOVER EFFECTS:
- Cards: Lift + shadow increase
- Buttons: Darken + scale 1.02
- Images: Zoom 1.05
- Links: Color change + underline

MICRO-INTERACTIONS:
- Button click: Ripple effect
- Form focus: Border glow
- Loading: Spinner or progress bar
- Success: Checkmark animation (Lottie)

BACKGROUND ELEMENTS:
- Subtle bubbles floating (CSS animation)
- Gradient mesh (animated)
- Wavy shapes (SVG, animated)


---

MEDIA ASSETS SUMMARY FOR LANDING PAGE
-----------------------------------

REQUIRED IMAGES:
1. Hero image: 1920x1080px (happy customer or modern facility)
2. Feature card images: 3 x 400x300px (mobile app, folded clothes, tracking)
3. Process illustrations: 4 x 200x200px SVG (book, pickup, clean, deliver)
4. Service photos: 3 x 600x400px (wash/fold, wash/iron, dry clean)
5. Customer avatars: 5 x 80x80px circular (diverse, authentic)
6. Phone mockup: 800x1600px (app interface)
7. App screenshots: 6-8 x 1080x1920px (various screens)

ICONS & GRAPHICS:
8. Service icons: 3 x 48x48px SVG (washing machine, iron, hanger)
9. Feature icons: 3 x 64x64px SVG (clock, shield, location)
10. Social media icons: 3 x 24x24px SVG (Facebook, Instagram, Twitter)
11. Payment logos: 4 x 32px height (GCash, Maya, Visa, Mastercard)
12. Store badges: 2 x official SVG (App Store, Google Play)
13. Star ratings: 16x16px SVG (gold)

ANIMATIONS (Optional):
14. Lottie animations: Success checkmark, loading spinner
15. Background elements: Floating bubbles (CSS/SVG)

TOTAL ASSETS: ~35-40 media files

===========================================
PART 1: AUTHENTICATION PAGES
===========================================

1. LOGIN PAGE
-----------------------------------
URL: /login

LAYOUT:
- Two-column layout (50/50 split on desktop, stacked on mobile)
- Left column: Branding with imagery
- Right column: Login form

LEFT COLUMN (BRANDING):
- Full-height background: Gradient from #2563EB to #14B8A6
- Background image overlay: Semi-transparent laundry-themed photo (washing machines, clean linens)
- WashMate logo (centered, white, 200px width)
- Tagline: "Your Laundry, Simplified" (white, below logo)
- Decorative elements: 
  * Animated washing machine illustration (Lottie JSON)
  * Floating soap bubbles (CSS animation)
  * Subtle pattern overlay (washing machine icons, very light)
- Responsive: Hidden on mobile, full screen on desktop

MEDIA ASSETS:
- Background photo: 1080x1920px, laundry facility interior
- Logo: White SVG version
- Washing machine animation: Lottie JSON (200x200px)
- Bubble graphics: 3-5 SVG circles (various sizes)

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

Banner/Hero Section:
- Background: Blue gradient with pattern
- Illustration: Happy person with fresh laundry (right side, 400x300px)
- Welcome message: "Welcome back, [User Name]!"
- Quick action button: "Book Laundry Service" (large, prominent)

MEDIA: Banner illustration (400x300px WebP), pattern overlay (SVG)

Stats Cards (3 columns):
Card 1: Active Orders
- Icon: Animated washing machine icon (64x64px Lottie or SVG)
- Number: "2" (large, blue)
- Label: "Active Orders"
- Background illustration: Subtle washing machine pattern
- Link: "View all →"

Card 2: Subscription Status
- Icon: Crown icon (64x64px SVG, gold)
- Text: "Weekly Plan" or "No Active Plan"
- Illustration: Subscription badge graphic (120x120px)
- Link: "Manage →"

Card 3: Wallet Balance
- Icon: Wallet icon (64x64px SVG)
- Amount: "₱500.00" (large)
- Illustration: Coins/money graphic (100x100px)
- Link: "Top up →"

MEDIA PER CARD: Icon (64x64px SVG), background illustration (optional)

Recent Orders Section:
- Order status badges with icons:
  * Pending: Clock icon (orange)
  * In Progress: Loader icon (blue, animated)
  * Completed: Checkmark (green)
  * Delivered: Package icon (teal)
- Empty state: Illustration of empty laundry basket (300x300px) + "No orders yet" message

MEDIA: Status icons (24x24px SVG), empty state illustration (300x300px)

Quick Service Selection:
- 3 service cards with photos:
  
  Card 1: Wash & Fold
  - Photo: Neatly folded clothes (400x300px)
  - Icon overlay: Washing machine (48px white)
  - Price tag: "₱35/kg"
  
  Card 2: Wash & Iron
  - Photo: Ironed dress shirts (400x300px)
  - Icon overlay: Iron (48px white)
  - Price tag: "₱45/kg"
  
  Card 3: Dry Clean
  - Photo: Suit on hanger (400x300px)
  - Icon overlay: Hanger (48px white)
  - Price tag: "₱150/pc"

MEDIA: Service photos (3 x 400x300px WebP), overlay icons (48x48px SVG)

DESIGN:
- Clean, minimal cards with photos
- Consistent spacing (24px between sections)
- Blue primary actions
- Light gray backgrounds
- Hover effects: Lift + shadow

MEDIA SUMMARY (Customer Dashboard):
- Banner illustration: 400x300px
- Status icons: 24x24px SVG (4 types)
- Empty state: 300x300px illustration
- Service photos: 3 x 400x300px
- Card icons: 64x64px SVG (3 types)


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

Shop Header Banner:
- Background: Shop cover photo (1200x300px, laundry facility)
- Shop logo: Circular (120x120px)
- Shop name (large, bold)
- Shop status badge: "Active" (green)
- Quick actions: "Update Hours" | "View Profile"

MEDIA: Cover photo (1200x300px), shop logo (120x120px)

Performance Cards (4 columns):
Card 1: Today's Orders
- Icon: Package icon with counter animation (64x64px)
- Number: "12" (large, blue, count-up animation)
- Subtext: "+3 from yesterday" (green arrow)
- Micro-illustration: Orders graphic

Card 2: In Progress
- Icon: Animated loader/spinner (64x64px Lottie)
- Number: "5" (orange)
- Mini chart: Trend line (SVG)

Card 3: Completed Today
- Icon: Animated checkmark (64x64px, green)
- Number: "3" (green)
- Confetti effect on hover

Card 4: Revenue Today
- Icon: Money/currency icon (64x64px)
- Amount: "₱3,250" (large, green)
- Sparkle animation
- Mini chart: Revenue trend

MEDIA: Icons (4 x 64x64px), animations (Lottie JSON)

Order Cards:
- Customer avatar: Circular photo or initials (48x48px)
- Service type icon: (24x24px)
- Map pin icon: For address (16x16px)
- Phone icon: Call button (20x20px)

Empty State:
- Illustration: Clipboard with checkmarks (300x300px)
- Message: "No pending orders"

Chart Section:
- Weekly orders: Bar chart with gradient fill
- Service distribution: Donut chart with icons
- Use: Recharts library with custom colors

MEDIA SUMMARY (Shop Owner Dashboard):
- Cover photo: 1200x300px
- Shop logo: 120x120px
- Performance icons: 4 x 64x64px
- Customer avatars: 48x48px each
- Empty state: 300x300px
- Chart graphics: SVG generated


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

Hero Section:
- System status graphic: Server/network illustration (500x200px)
- Real-time metrics: Animated counters
- Background: Subtle grid pattern (SVG)

Key Metrics Grid (4 columns):
Card 1: Total Users
- Icon: Users group icon (64x64px, animated on load)
- Number: "1,234" (large, count-up animation)
- Subtext: "+56 this month" (green, arrow up)
- Mini avatar stack: Recent users (5 x 32px overlapping)
- User type breakdown icons

Card 2: Total Revenue
- Icon: Currency/money bag (64x64px)
- Amount: "₱125,450" (large, green, count-up)
- Trend: Sparkline chart (small, inline)
- Arrow: "+12.5%" (green, animated)

Card 3: Active Orders
- Icon: Package with notification badge (64x64px)
- Number: "47" (blue)
- Status breakdown: Mini donut chart
- Pulse animation on live updates

Card 4: System Health
- Icon: Heart monitor/activity (64x64px, animated pulse)
- Status: "All Systems Operational" (green)
- Server uptime: "99.9%"
- Link: "View details"

MEDIA: Icons (4 x 64x64px), mini charts (inline SVG), avatar stack

Charts Section:
LEFT: Revenue Trends Chart
- Full-width line chart with gradient fill
- Data points: Last 30 days
- Interactive tooltips with revenue amounts
- Toggle buttons: Daily | Weekly | Monthly
- Custom blue/teal gradient

RIGHT: Service Distribution
- Donut/pie chart with custom colors
- Service type icons inside segments
- Percentages labeled
- Legend with icons

MEDIA: Chart graphics (Recharts/Chart.js), service icons in chart

Activity Feed:
- Event type icons: (24x24px)
  * User joined: User plus icon
  * Order completed: Check circle
  * Payment received: Money icon
  * Feedback: Star icon
- User avatars: 32x32px circular
- Timestamp icons: Clock (16x16px)

Quick Actions Panel:
- Action cards with illustrations:
  * Add User: Person with plus (120x120px)
  * Update Pricing: Price tag (120x120px)
  * View Feedback: Speech bubble (120x120px)
  * Generate Report: Document (120x120px)

MEDIA SUMMARY (Admin Dashboard):
- Hero illustration: 500x200px
- Metric icons: 4 x 64x64px
- Event icons: 24x24px SVG (various)
- User avatars: 32x32px
- Action illustrations: 4 x 120x120px
- Charts: Dynamic SVG

DESIGN:
- Professional business aesthetic
- Data visualization prominent
- Purple/blue color scheme for admin
- Clean information hierarchy
- Animated data updates


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

PAGES:
1. Landing Page (fully functional, responsive)
2. Login Page (fully functional)
3. Register Page (with validation)
4. Email Verification Page
5. Customer Dashboard (with mock data & media)
6. Shop Owner Dashboard (with mock data & media)
7. Admin Dashboard (with mock data & media)

COMPONENTS:
8. Shared components library
9. Protected routing setup
10. JWT authentication flow

MEDIA ASSETS:
11. All images optimized (WebP + fallbacks)
12. SVG icons library
13. Lottie animations (loading, success states)
14. Placeholder images for development
15. Image optimization setup

FEATURES:
16. Responsive design (mobile/tablet/desktop)
17. Scroll animations (AOS or Framer Motion)
18. Lazy loading for images
19. Accessibility compliance (WCAG AA)
20. Performance optimized (Lighthouse score 90+)


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
- [ ] Fast page loads (< 3s)
- [ ] Smooth animations (60fps)
- [ ] No layout shifts (CLS < 0.1)
- [ ] Optimized images (lazy loading)
- [ ] Lighthouse score 90+ (performance)

Media & Images:
- [ ] All images load correctly
- [ ] WebP format with fallbacks
- [ ] Lazy loading works
- [ ] Responsive images (srcset)
- [ ] Icons display properly (SVG)
- [ ] Animations smooth (Lottie/CSS)
- [ ] No broken image links
- [ ] Alt text on all images

Landing Page:
- [ ] Hero section loads fast
- [ ] Scroll animations work
- [ ] Testimonials carousel functional
- [ ] Download buttons link correctly
- [ ] All sections responsive
- [ ] Footer links work
- [ ] CTA buttons functional

Accessibility:
- [ ] Keyboard navigation works
- [ ] Screen reader compatible
- [ ] Sufficient color contrast
- [ ] Focus indicators visible
- [ ] Alt text on images
- [ ] ARIA labels correct


This is a complete, production-ready frontend with landing page, authentication, dashboards, and comprehensive media integration! 🎨✨