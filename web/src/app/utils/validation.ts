import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  rememberMe: z.boolean().optional(),
});

export const registerSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters').refine(
    (val) => val.trim().split(' ').length >= 2,
    'Please enter your full name (first and last name)'
  ),
  email: z.string().email('Invalid email address'),
  phone: z.string().regex(/^\+63\s?\d{3}\s?\d{3}\s?\d{4}$/, 'Invalid Philippine phone number format'),
  password: z.string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[0-9]/, 'Password must contain at least one number'),
  confirmPassword: z.string(),
  acceptTerms: z.boolean().refine((val) => val === true, 'You must accept the terms and conditions'),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

export const getPasswordStrength = (password: string): {
  strength: 'weak' | 'medium' | 'strong';
  color: string;
  percentage: number;
} => {
  let strength = 0;
  
  if (password.length >= 8) strength++;
  if (password.length >= 12) strength++;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;
  if (/[0-9]/.test(password)) strength++;
  if (/[^a-zA-Z0-9]/.test(password)) strength++;

  if (strength <= 2) {
    return { strength: 'weak', color: '#EF4444', percentage: 33 };
  } else if (strength <= 3) {
    return { strength: 'medium', color: '#F59E0B', percentage: 66 };
  } else {
    return { strength: 'strong', color: '#10B981', percentage: 100 };
  }
};
