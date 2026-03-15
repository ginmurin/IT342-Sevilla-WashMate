import { useRef, useState, useEffect } from "react";
import { Link, Navigate } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import { Button } from "../components/Button";
import {
  Droplets,
  ShieldCheck,
  Clock,
  Star,
  Sparkles,
  ChevronRight,
  Play,
  MapPin,
  Smartphone,
  Truck,
  WashingMachine,
  CheckCircle2,
  Quote,
  ChevronLeft,
  Facebook,
  Instagram,
  Twitter,
  Mail,
  Phone,
  MapPinned,
  ArrowRight,
  Zap,
  Check,
} from "lucide-react";
import { motion } from "motion/react";
import { ImageWithFallback } from "../components/figma/ImageWithFallback";

// Images
const HERO_IMG = "https://i.imgur.com/9L2yFK9.jpeg";
const FEATURE_1 = "https://images.unsplash.com/photo-1758876021591-4a48ab8968b6?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwZXJzb24lMjB1c2luZyUyMHNtYXJ0cGhvbmUlMjBhcHAlMjBtb2Rlcm58ZW58MXx8fHwxNzcyOTgwNDI0fDA&ixlib=rb-4.1.0&q=80&w=1080";
const FEATURE_2 = "https://images.unsplash.com/photo-1574057675080-6cdfd3225424?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxmb2xkZWQlMjBjbG90aGVzJTIwbmVhdCUyMG9yZ2FuaXplZCUyMGNvbG9yZnVsfGVufDF8fHx8MTc3Mjk4MDQzMHww&ixlib=rb-4.1.0&q=80&w=1080";
const FEATURE_3 = "https://i.imgur.com/g06jOke.jpeg";
const SERVICE_1 = "https://images.unsplash.com/photo-1654977810620-b68f508f3ac0?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjbGVhbiUyMGZvbGRlZCUyMGxhdW5kcnklMjB0b3dlbHMlMjB3aGl0ZXxlbnwxfHx8fDE3NzI5ODA0MjV8MA&ixlib=rb-4.1.0&q=80&w=1080";
const SERVICE_2 = "https://images.unsplash.com/photo-1603251579431-8041402bdeda?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxpcm9uZWQlMjBkcmVzcyUyMHNoaXJ0cyUyMGhhbmdpbmclMjBwcm9mZXNzaW9uYWx8ZW58MXx8fHwxNzcyOTgwNDI2fDA&ixlib=rb-4.1.0&q=80&w=1080";
const SERVICE_3 = "https://images.unsplash.com/photo-1665411887939-a4ee3487c680?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzdWl0JTIwZHJ5JTIwY2xlYW5pbmclMjBoYW5nZXIlMjBwcm9mZXNzaW9uYWx8ZW58MXx8fHwxNzcyOTgwNDI2fDA&ixlib=rb-4.1.0&q=80&w=1080";
const AVATAR_1 = "https://images.unsplash.com/photo-1762522921456-cdfe882d36c3?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx5b3VuZyUyMHByb2Zlc3Npb25hbCUyMHdvbWFuJTIwcG9ydHJhaXQlMjBzbWlsaW5nfGVufDF8fHx8MTc3Mjg4NzM0OXww&ixlib=rb-4.1.0&q=80&w=1080";
const AVATAR_2 = "https://images.unsplash.com/photo-1723537742563-15c3d351dbf2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxidXNpbmVzcyUyMG1hbiUyMHBvcnRyYWl0JTIwaGVhZHNob3QlMjBjYXN1YWx8ZW58MXx8fHwxNzcyOTgwNDMxfDA&ixlib=rb-4.1.0&q=80&w=1080";
const AVATAR_3 = "https://images.unsplash.com/photo-1758600433358-b44bf8a32c8f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtaWRkbGUlMjBhZ2VkJTIwd29tYW4lMjBwb3J0cmFpdCUyMHNtaWxpbmclMjB3YXJtfGVufDF8fHx8MTc3Mjk4MDQyN3ww&ixlib=rb-4.1.0&q=80&w=1080";
const AVATAR_4 = "https://images.unsplash.com/photo-1758600587839-56ba05596c69?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxhc2lhbiUyMHdvbWFuJTIwcG9ydHJhaXQlMjBmcmllbmRseSUyMGNhc3VhbHxlbnwxfHx8fDE3NzI5ODA0MzF8MA&ixlib=rb-4.1.0&q=80&w=1080";

// Floating bubble component
function FloatingBubbles() {
  return (
    <div className="absolute inset-0 overflow-hidden pointer-events-none">
      {[...Array(8)].map((_, i) => (
        <motion.div
          key={i}
          className="absolute rounded-full bg-blue-400/10 border border-blue-300/10"
          style={{
            width: 20 + Math.random() * 60,
            height: 20 + Math.random() * 60,
            left: `${Math.random() * 100}%`,
            bottom: `-${20 + Math.random() * 40}px`,
          }}
          animate={{
            y: [0, -800 - Math.random() * 400],
            x: [0, (Math.random() - 0.5) * 100],
            opacity: [0, 0.6, 0],
          }}
          transition={{
            duration: 8 + Math.random() * 8,
            repeat: Infinity,
            delay: Math.random() * 6,
            ease: "easeOut",
          }}
        />
      ))}
    </div>
  );
}

export function Home() {
  const { user, isAuthenticated } = useAuth();
  const containerRef = useRef<HTMLDivElement>(null);
  const [activeTestimonial, setActiveTestimonial] = useState(0);

  const ease = [0.16, 1, 0.3, 1] as const;

  const features = [
    {
      icon: <Zap className="w-7 h-7" />,
      title: "Same-Day Service",
      description: "Order before 10 AM, get your laundry back the same day. Lightning-fast turnaround.",
      badge: "24/7 Booking Available",
      image: FEATURE_1,
      color: "bg-blue-500",
    },
    {
      icon: <ShieldCheck className="w-7 h-7" />,
      title: "Premium Quality",
      description: "Expert cleaning with eco-friendly detergents and careful handling of every garment.",
      badge: "100% Satisfaction Guaranteed",
      image: FEATURE_2,
      color: "bg-teal-500",
    },
    {
      icon: <MapPin className="w-7 h-7" />,
      title: "Real-Time Tracking",
      description: "Know exactly where your laundry is from pickup to delivery with live updates.",
      badge: "Live Updates",
      image: FEATURE_3,
      color: "bg-blue-600",
    },
  ];

  const steps = [
    {
      num: "01",
      icon: <Smartphone className="w-10 h-10" />,
      title: "Book Your Service",
      description: "Choose your service type, schedule pickup time, and confirm your address.",
      color: "text-teal-500",
      bg: "bg-teal-50",
    },
    {
      num: "02",
      icon: <Truck className="w-10 h-10" />,
      title: "Free Pickup",
      description: "Our professional staff collects your laundry at your chosen time.",
      color: "text-blue-600",
      bg: "bg-blue-50",
    },
    {
      num: "03",
      icon: <WashingMachine className="w-10 h-10" />,
      title: "Expert Cleaning",
      description: "Your clothes are professionally washed, dried, and folded with care.",
      color: "text-teal-500",
      bg: "bg-teal-50",
    },
    {
      num: "04",
      icon: <CheckCircle2 className="w-10 h-10" />,
      title: "Fresh & Delivered",
      description: "Receive your clean, fresh laundry at your doorstep within 24 hours.",
      color: "text-blue-600",
      bg: "bg-blue-50",
    },
  ];

  const services = [
    {
      title: "Wash & Fold",
      price: "P35/kg",
      image: SERVICE_1,
      features: ["Same-day service available", "Eco-friendly detergents", "Sorted by color and fabric", "Neatly folded and packaged"],
      popular: false,
    },
    {
      title: "Wash & Iron",
      price: "P45/kg",
      image: SERVICE_2,
      features: ["Professional pressing", "Wrinkle-free guarantee", "Hangers included", "Same-day express available"],
      popular: true,
    },
    {
      title: "Dry Cleaning",
      price: "P150/piece",
      image: SERVICE_3,
      features: ["Delicate fabric care", "Stain removal specialists", "Premium finishing", "48-hour service"],
      popular: false,
    },
  ];

  const testimonials = [
    {
      quote: "WashMate saved me so much time! The quality is amazing and the app makes everything super easy. Highly recommend to any busy professional!",
      name: "Maria Santos",
      role: "Marketing Manager",
      location: "Cebu City",
      avatar: AVATAR_1,
      rating: 5,
    },
    {
      quote: "As a parent of three, laundry used to be my biggest headache. WashMate handles it all perfectly. The pickup and delivery is a game changer!",
      name: "James Rivera",
      role: "Business Owner",
      location: "Manila",
      avatar: AVATAR_2,
      rating: 5,
    },
    {
      quote: "I've been using WashMate for 6 months and the consistency is remarkable. My uniforms always come back perfect. Great value for money!",
      name: "Rosa Chen",
      role: "Restaurant Owner",
      location: "Davao City",
      avatar: AVATAR_3,
      rating: 5,
    },
    {
      quote: "The real-time tracking feature is fantastic. I always know exactly when my clothes will arrive. The quality of cleaning is top-notch too.",
      name: "Lisa Tan",
      role: "Software Engineer",
      location: "Quezon City",
      avatar: AVATAR_4,
      rating: 5,
    },
  ];

  // Auto-rotate testimonials
  useEffect(() => {
    const timer = setInterval(() => {
      setActiveTestimonial((prev) => (prev + 1) % testimonials.length);
    }, 5000);
    return () => clearInterval(timer);
  }, [testimonials.length]);

  const stagger = {
    hidden: { opacity: 0 },
    show: { opacity: 1, transition: { staggerChildren: 0.15 } },
  };
  const fadeUp = {
    hidden: { opacity: 0, y: 30 },
    show: { opacity: 1, y: 0, transition: { duration: 0.6, ease } },
  };

  if (isAuthenticated && user) {
    if (user.role === "CUSTOMER") return <Navigate to="/customer" replace />;
    if (user.role === "SHOPOWNER") return <Navigate to="/shop" replace />;
    if (user.role === "ADMIN") return <Navigate to="/admin" replace />;
  }

  return (
    <div className="flex-1 flex flex-col w-full -mt-16 relative" ref={containerRef}>
      {/* ===== HERO SECTION ===== */}
      <section className="relative w-full min-h-screen flex items-center overflow-hidden bg-gradient-to-br from-gray-900 via-blue-950 to-gray-900">
        <FloatingBubbles />
        <div className="absolute inset-0 z-0 opacity-20 bg-gradient-to-r from-blue-600/30 to-teal-500/30" />

        <div className="relative z-10 container mx-auto px-4 sm:px-6 lg:px-8 pt-24 pb-16">
          <div className="grid lg:grid-cols-2 gap-12 lg:gap-16 items-center">
            {/* Left Content */}
            <div className="order-2 lg:order-1">
              <motion.p
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, ease }}
                className="inline-flex items-center gap-2 rounded-full bg-teal-500/10 border border-teal-500/20 px-4 py-1.5 text-teal-400 mb-6"
              >
                <Sparkles className="w-4 h-4" />
                LAUNDRY SERVICE MADE SIMPLE
              </motion.p>

              <motion.h1
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.7, delay: 0.1, ease }}
                className="text-4xl sm:text-5xl lg:text-6xl xl:text-7xl font-extrabold text-white mb-6 leading-[1.1] tracking-tight"
              >
                Your Laundry,{" "}
                <span className="bg-gradient-to-r from-blue-400 to-teal-400 bg-clip-text text-transparent">
                  Delivered Fresh
                </span>{" "}
                & Folded
              </motion.h1>

              <motion.p
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.7, delay: 0.2, ease }}
                className="text-lg text-gray-400 mb-8 max-w-lg"
              >
                Book professional laundry services in seconds. Get your clothes picked up, cleaned, and delivered back to you.
              </motion.p>

              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.7, delay: 0.3, ease }}
                className="flex flex-col sm:flex-row gap-4 mb-10"
              >
                <Link to="/register">
                  <Button size="lg" className="w-full sm:w-auto h-14 px-8 bg-blue-600 hover:bg-blue-500 text-white shadow-lg shadow-blue-600/25 transition-all duration-300 hover:scale-[1.02]">
                    Book Now
                    <ChevronRight className="ml-2 h-5 w-5" />
                  </Button>
                </Link>
                <Link to="/login">
                  <Button size="lg" className="w-full sm:w-auto h-14 px-8 bg-white/5 border border-white/10 text-white hover:bg-white/10 backdrop-blur-sm transition-all duration-300">
                    <Play className="mr-2 h-4 w-4" />
                    Watch Demo
                  </Button>
                </Link>
              </motion.div>

              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.7, delay: 0.5, ease }}
                className="flex items-center gap-3"
              >
                <div className="flex -space-x-2">
                  {[AVATAR_1, AVATAR_2, AVATAR_3].map((src, i) => (
                    <ImageWithFallback
                      key={i}
                      src={src}
                      alt="Customer"
                      className="w-8 h-8 rounded-full border-2 border-gray-900 object-cover"
                    />
                  ))}
                </div>
                <div className="flex items-center gap-1">
                  {[...Array(5)].map((_, i) => (
                    <Star key={i} className="w-4 h-4 text-amber-400 fill-amber-400" />
                  ))}
                </div>
                <span className="text-gray-400 text-sm">4.9/5 from 1,200+ customers</span>
              </motion.div>
            </div>

            {/* Right Image */}
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.2, ease }}
              className="order-1 lg:order-2 relative"
            >
              <div className="relative rounded-3xl overflow-hidden shadow-2xl shadow-blue-500/10">
                <ImageWithFallback
                  src={HERO_IMG}
                  alt="Professional laundry service delivery"
                  className="w-full h-[400px] lg:h-[520px] object-cover"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-gray-900/60 via-transparent to-transparent" />
              </div>
              {/* Floating stat card */}
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.8, ease }}
                className="absolute -bottom-4 -left-4 bg-white/10 backdrop-blur-xl rounded-2xl p-4 border border-white/10 shadow-xl"
              >
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-teal-500/20 rounded-xl flex items-center justify-center">
                    <Clock className="w-5 h-5 text-teal-400" />
                  </div>
                  <div>
                    <p className="text-white text-sm font-semibold">24hr Turnaround</p>
                    <p className="text-gray-400 text-xs">Express available</p>
                  </div>
                </div>
              </motion.div>
              <motion.div
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 1, ease }}
                className="absolute -top-4 -right-4 bg-white/10 backdrop-blur-xl rounded-2xl p-4 border border-white/10 shadow-xl"
              >
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-blue-500/20 rounded-xl flex items-center justify-center">
                    <ShieldCheck className="w-5 h-5 text-blue-400" />
                  </div>
                  <div>
                    <p className="text-white text-sm font-semibold">Quality Assured</p>
                    <p className="text-gray-400 text-xs">100% guarantee</p>
                  </div>
                </div>
              </motion.div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* ===== FEATURES SECTION ===== */}
      <section id="features" className="w-full py-24 bg-gray-50 relative overflow-hidden">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, margin: "-80px" }}
            transition={{ duration: 0.6, ease }}
            className="text-center mb-16"
          >
            <p className="text-blue-600 font-semibold mb-2">WHY CHOOSE US</p>
            <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 mb-4">
              Why Choose WashMate?
            </h2>
            <p className="text-gray-500 max-w-2xl mx-auto">
              Professional laundry service at your fingertips. We combine technology with premium local care.
            </p>
          </motion.div>

          <motion.div
            variants={stagger}
            initial="hidden"
            whileInView="show"
            viewport={{ once: true, margin: "-50px" }}
            className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto"
          >
            {features.map((feature, index) => (
              <motion.div
                key={index}
                variants={fadeUp}
                whileHover={{ y: -8 }}
                className="group bg-white rounded-2xl overflow-hidden shadow-sm border border-gray-100 transition-all duration-300 hover:shadow-xl"
              >
                <div className="relative h-48 overflow-hidden">
                  <ImageWithFallback
                    src={feature.image}
                    alt={feature.title}
                    className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent" />
                  <div className={`absolute top-4 left-4 w-12 h-12 ${feature.color} rounded-xl flex items-center justify-center text-white shadow-lg`}>
                    {feature.icon}
                  </div>
                </div>
                <div className="p-6">
                  <h3 className="text-xl font-bold text-gray-900 mb-2">{feature.title}</h3>
                  <p className="text-gray-500 mb-4 leading-relaxed">{feature.description}</p>
                  <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-blue-600 bg-blue-50 px-3 py-1.5 rounded-full">
                    <Sparkles className="w-3 h-3" />
                    {feature.badge}
                  </span>
                </div>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* ===== HOW IT WORKS ===== */}
      <section id="how-it-works" className="w-full py-24 bg-white relative overflow-hidden">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, margin: "-80px" }}
            transition={{ duration: 0.6, ease }}
            className="text-center mb-16"
          >
            <p className="text-teal-500 font-semibold mb-2">HOW IT WORKS</p>
            <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 mb-4">
              Simple as 1-2-3-4
            </h2>
            <p className="text-gray-500 max-w-2xl mx-auto">
              Getting your laundry done has never been easier. Four simple steps to fresh, clean clothes.
            </p>
          </motion.div>

          <motion.div
            variants={stagger}
            initial="hidden"
            whileInView="show"
            viewport={{ once: true, margin: "-50px" }}
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8 max-w-6xl mx-auto"
          >
            {steps.map((step, index) => (
              <motion.div
                key={index}
                variants={fadeUp}
                className="relative text-center group"
              >
                {/* Connector line */}
                {index < steps.length - 1 && (
                  <div className="hidden lg:block absolute top-16 left-[60%] w-[80%] border-t-2 border-dashed border-gray-200 z-0" />
                )}
                <div className="relative z-10">
                  <div className={`w-20 h-20 ${step.bg} rounded-2xl flex items-center justify-center mx-auto mb-4 ${step.color} transition-transform duration-300 group-hover:scale-110`}>
                    {step.icon}
                  </div>
                  <span className={`text-5xl font-extrabold ${step.color} opacity-15 absolute -top-2 right-2 lg:right-auto lg:left-1/2 lg:-translate-x-1/2`}>
                    {step.num}
                  </span>
                  <h3 className="text-lg font-bold text-gray-900 mb-2">{step.title}</h3>
                  <p className="text-gray-500 text-sm leading-relaxed">{step.description}</p>
                </div>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* ===== SERVICES SECTION ===== */}
      <section id="services" className="w-full py-24 bg-gray-50 relative overflow-hidden">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, margin: "-80px" }}
            transition={{ duration: 0.6, ease }}
            className="text-center mb-16"
          >
            <p className="text-blue-600 font-semibold mb-2">OUR SERVICES</p>
            <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 mb-4">
              Professional care for every fabric
            </h2>
            <p className="text-gray-500 max-w-2xl mx-auto">
              From everyday wear to delicate garments, we handle it all with expert precision.
            </p>
          </motion.div>

          <motion.div
            variants={stagger}
            initial="hidden"
            whileInView="show"
            viewport={{ once: true, margin: "-50px" }}
            className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto"
          >
            {services.map((service, index) => (
              <motion.div
                key={index}
                variants={fadeUp}
                whileHover={{ y: -8 }}
                className={`relative bg-white rounded-2xl overflow-hidden shadow-sm border transition-all duration-300 hover:shadow-xl ${service.popular ? "border-blue-200 ring-2 ring-blue-500/20" : "border-gray-100"}`}
              >
                {service.popular && (
                  <div className="absolute top-4 right-4 z-20 bg-amber-500 text-white text-xs font-bold px-3 py-1 rounded-full shadow-lg">
                    Most Popular
                  </div>
                )}
                <div className="relative h-56 overflow-hidden">
                  <ImageWithFallback
                    src={service.image}
                    alt={service.title}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
                  <div className="absolute bottom-4 left-4">
                    <h3 className="text-xl font-bold text-white">{service.title}</h3>
                    <p className="text-white/80 text-sm">Starting at <span className="text-white font-bold text-lg">{service.price}</span></p>
                  </div>
                </div>
                <div className="p-6">
                  <ul className="space-y-3 mb-6">
                    {service.features.map((feat, i) => (
                      <li key={i} className="flex items-center gap-2 text-gray-600 text-sm">
                        <Check className="w-4 h-4 text-green-500 flex-shrink-0" />
                        {feat}
                      </li>
                    ))}
                  </ul>
                  <Link to="/services">
                    <Button className="w-full bg-blue-600 hover:bg-blue-500 text-white transition-all duration-300">
                      Book Now
                      <ArrowRight className="ml-2 w-4 h-4" />
                    </Button>
                  </Link>
                </div>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* ===== TESTIMONIALS SECTION ===== */}
      <section className="w-full py-24 bg-white relative overflow-hidden">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, margin: "-80px" }}
            transition={{ duration: 0.6, ease }}
            className="text-center mb-16"
          >
            <p className="text-teal-500 font-semibold mb-2">TESTIMONIALS</p>
            <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 mb-4">
              What Our Customers Say
            </h2>
            <p className="text-gray-500 max-w-2xl mx-auto">
              Don't just take our word for it. Here's what real customers have to say about WashMate.
            </p>
          </motion.div>

          <div className="max-w-4xl mx-auto">
            <div className="relative">
              {testimonials.map((t, index) => (
                <motion.div
                  key={index}
                  initial={false}
                  animate={{
                    opacity: activeTestimonial === index ? 1 : 0,
                    scale: activeTestimonial === index ? 1 : 0.95,
                  }}
                  transition={{ duration: 0.5, ease }}
                  className={`bg-gray-50 rounded-3xl p-8 md:p-12 border border-gray-100 ${
                    activeTestimonial === index
                      ? "relative"
                      : "absolute inset-0 pointer-events-none"
                  }`}
                >
                  <Quote className="w-10 h-10 text-blue-200 mb-6" />
                  <div className="flex gap-1 mb-4">
                    {[...Array(t.rating)].map((_, i) => (
                      <Star key={i} className="w-5 h-5 text-amber-400 fill-amber-400" />
                    ))}
                  </div>
                  <p className="text-gray-700 text-lg md:text-xl leading-relaxed mb-8">
                    "{t.quote}"
                  </p>
                  <div className="flex items-center gap-4">
                    <ImageWithFallback
                      src={t.avatar}
                      alt={t.name}
                      className="w-14 h-14 rounded-full object-cover ring-2 ring-blue-100"
                    />
                    <div>
                      <p className="font-bold text-gray-900 flex items-center gap-2">
                        {t.name}
                        <CheckCircle2 className="w-4 h-4 text-blue-500" />
                      </p>
                      <p className="text-gray-500 text-sm">{t.role} · {t.location}</p>
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>

            {/* Testimonial Controls */}
            <div className="flex items-center justify-center gap-4 mt-8">
              <button
                onClick={() => setActiveTestimonial((prev) => (prev - 1 + testimonials.length) % testimonials.length)}
                className="w-10 h-10 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors"
              >
                <ChevronLeft className="w-5 h-5 text-gray-600" />
              </button>
              <div className="flex gap-2">
                {testimonials.map((_, i) => (
                  <button
                    key={i}
                    onClick={() => setActiveTestimonial(i)}
                    className={`h-2 rounded-full transition-all duration-300 ${activeTestimonial === i ? "w-8 bg-blue-600" : "w-2 bg-gray-300"}`}
                  />
                ))}
              </div>
              <button
                onClick={() => setActiveTestimonial((prev) => (prev + 1) % testimonials.length)}
                className="w-10 h-10 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors"
              >
                <ChevronRight className="w-5 h-5 text-gray-600" />
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* ===== DOWNLOAD APP / CTA SECTION ===== */}
      <section className="w-full py-24 bg-gradient-to-br from-blue-600 to-teal-500 relative overflow-hidden">
        <FloatingBubbles />
        <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="grid lg:grid-cols-2 gap-12 items-center max-w-6xl mx-auto">
            <motion.div
              initial={{ opacity: 0, x: -30 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.7, ease }}
            >
              <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-white mb-6">
                Get the WashMate App
              </h2>
              <p className="text-blue-100 text-lg mb-8">
                Book services, track orders, and manage your account on the go.
              </p>
              <div className="space-y-4 mb-10">
                {[
                  "Easy booking in seconds",
                  "Real-time order tracking",
                  "Secure wallet payments",
                  "Exclusive app-only deals",
                ].map((item, i) => (
                  <div key={i} className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-white/20 rounded-lg flex items-center justify-center">
                      <Check className="w-4 h-4 text-white" />
                    </div>
                    <span className="text-white">{item}</span>
                  </div>
                ))}
              </div>
              <div className="flex flex-col sm:flex-row gap-4">
                <Link to="/register">
                  <Button size="lg" className="w-full sm:w-auto h-14 px-8 bg-white text-blue-600 hover:bg-gray-100 shadow-lg transition-all duration-300 hover:scale-[1.02]">
                    Get Started Free
                    <ArrowRight className="ml-2 w-5 h-5" />
                  </Button>
                </Link>
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: 30 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.7, delay: 0.2, ease }}
              className="relative flex items-center justify-center"
            >
              {/* Phone mockup */}
              <div className="relative w-64 h-[500px] bg-gray-900 rounded-[3rem] p-3 shadow-2xl border border-gray-700">
                <div className="absolute top-0 left-1/2 -translate-x-1/2 w-32 h-6 bg-gray-900 rounded-b-2xl z-10" />
                <div className="w-full h-full bg-gradient-to-b from-blue-600 to-teal-500 rounded-[2.3rem] overflow-hidden flex flex-col items-center justify-center px-4">
                  <Droplets className="w-16 h-16 text-white mb-4" />
                  <p className="text-white font-bold text-xl mb-1">WashMate</p>
                  <p className="text-white/70 text-sm mb-8">Your Laundry, Simplified</p>
                  <div className="w-full space-y-3">
                    <div className="bg-white/20 rounded-xl p-3 backdrop-blur-sm">
                      <div className="flex items-center gap-2">
                        <WashingMachine className="w-5 h-5 text-white" />
                        <span className="text-white text-sm font-medium">Wash & Fold</span>
                        <span className="ml-auto text-white/80 text-xs">In Progress</span>
                      </div>
                    </div>
                    <div className="bg-white/20 rounded-xl p-3 backdrop-blur-sm">
                      <div className="flex items-center gap-2">
                        <Truck className="w-5 h-5 text-white" />
                        <span className="text-white text-sm font-medium">Out for Delivery</span>
                        <span className="ml-auto text-white/80 text-xs">ETA 2:30 PM</span>
                      </div>
                    </div>
                    <div className="bg-white/20 rounded-xl p-3 backdrop-blur-sm">
                      <div className="flex items-center gap-2">
                        <CheckCircle2 className="w-5 h-5 text-green-300" />
                        <span className="text-white text-sm font-medium">Delivered</span>
                        <span className="ml-auto text-white/80 text-xs">Yesterday</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* ===== FOOTER ===== */}
      <footer className="w-full bg-gray-900 text-gray-400 pt-16 pb-8">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-12 mb-12">
            {/* Brand */}
            <div>
              <div className="flex items-center gap-2 text-white mb-4">
                <Droplets className="w-6 h-6 text-teal-400" />
                <span className="text-xl font-bold">WashMate</span>
              </div>
              <p className="text-gray-500 mb-6">Your Laundry, Simplified</p>
              <div className="flex gap-3">
                {[Facebook, Instagram, Twitter].map((Icon, i) => (
                  <a key={i} href="#" className="w-10 h-10 bg-gray-800 rounded-lg flex items-center justify-center text-gray-500 hover:text-blue-400 hover:bg-gray-700 transition-all duration-200">
                    <Icon className="w-5 h-5" />
                  </a>
                ))}
              </div>
            </div>

            {/* Quick Links */}
            <div>
              <h4 className="text-white font-semibold mb-4">Quick Links</h4>
              <ul className="space-y-3">
                {["About Us", "How It Works", "Services", "Pricing", "Contact Us"].map((link) => (
                  <li key={link}>
                    <a href="#" className="hover:text-white transition-colors">{link}</a>
                  </li>
                ))}
              </ul>
            </div>

            {/* Support */}
            <div>
              <h4 className="text-white font-semibold mb-4">Support</h4>
              <ul className="space-y-3">
                {["Help Center", "FAQs", "Terms of Service", "Privacy Policy", "Refund Policy"].map((link) => (
                  <li key={link}>
                    <a href="#" className="hover:text-white transition-colors">{link}</a>
                  </li>
                ))}
              </ul>
            </div>

            {/* Contact */}
            <div>
              <h4 className="text-white font-semibold mb-4">Contact</h4>
              <ul className="space-y-3">
                <li className="flex items-center gap-2">
                  <Mail className="w-4 h-4 text-gray-500" />
                  support@washmate.ph
                </li>
                <li className="flex items-center gap-2">
                  <Phone className="w-4 h-4 text-gray-500" />
                  +63 912 345 6789
                </li>
                <li className="flex items-center gap-2">
                  <MapPinned className="w-4 h-4 text-gray-500" />
                  Cebu City, Philippines
                </li>
                <li className="flex items-center gap-2">
                  <Clock className="w-4 h-4 text-gray-500" />
                  Mon-Sun, 7 AM - 9 PM
                </li>
              </ul>
            </div>
          </div>

          <div className="border-t border-gray-800 pt-8 flex flex-col md:flex-row items-center justify-between gap-4">
            <p className="text-gray-600 text-sm">&copy; 2026 WashMate. All rights reserved.</p>
            <div className="flex gap-4 text-xs text-gray-600">
              <span className="bg-gray-800 px-3 py-1.5 rounded">GCash</span>
              <span className="bg-gray-800 px-3 py-1.5 rounded">Maya</span>
              <span className="bg-gray-800 px-3 py-1.5 rounded">Visa</span>
              <span className="bg-gray-800 px-3 py-1.5 rounded">Mastercard</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}