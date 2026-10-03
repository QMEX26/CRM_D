export type SubscriptionStatusType =
  | 'FREE_TRIAL'
  | 'ACTIVE'
  | 'EXPIRED'
  | 'CANCELLED'
  | 'NONE';

export interface SubscriptionPlan {
  id: number;
  name: string;
  targetRole: string; // 'ROLE_ADMIN' | 'ROLE_USER'
  price: number;
  currency: string;
  billingCycle: string;
  trialDays: number;
  active: boolean;
}

export interface Subscription {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  userRole: string;
  plan: SubscriptionPlan;
  status: SubscriptionStatusType;
  subscriptionType?: 'FREE_TRIAL' | 'PAID' | 'EXPIRED' | 'CANCELLED' | 'NONE';
  isTrialActive?: boolean;
  trialStartDate?: string;
  trialEndDate?: string;
  trialDaysRemaining?: number;
  trialStartAt?: string;
  trialEndAt?: string;
  currentPeriodStart?: string;
  currentPeriodEnd?: string;
  daysRemaining: number;
  isTrial: boolean;
  isActive: boolean;
  isExpired: boolean;
}

export interface RazorpayOrderResponse {
  orderId: string;
  amountInPaise: number;
  amount: number;
  currency: string;
  keyId: string;
  planName: string;
  userEmail: string;
  userName: string;
  userPhone?: string;
}

export interface VerifyPaymentRequest {
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}
