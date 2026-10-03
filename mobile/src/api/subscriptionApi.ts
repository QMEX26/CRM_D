import { apiClient } from './client';
import {
  Subscription,
  SubscriptionPlan,
  RazorpayOrderResponse,
  VerifyPaymentRequest,
} from '../types/subscription';

export const subscriptionApi = {
  /**
   * Fetch current authenticated user's subscription status & trial details
   */
  getMySubscription: async (): Promise<Subscription> => {
    const response = await apiClient.get<{ success: boolean; data: Subscription }>(
      '/subscription/my'
    );
    return response.data.data;
  },

  /**
   * Alias for getMySubscription (/subscription/status)
   */
  getStatus: async (): Promise<Subscription> => {
    const response = await apiClient.get<{ success: boolean; data: Subscription }>(
      '/subscription/status'
    );
    return response.data.data;
  },

  /**
   * Fetch all active subscription plans
   */
  getPlans: async (): Promise<SubscriptionPlan[]> => {
    const response = await apiClient.get<{ success: boolean; data: SubscriptionPlan[] }>(
      '/subscription/plans'
    );
    return response.data.data;
  },

  /**
   * Fetch the specific plan applicable for the current user's role
   */
  getMyPlan: async (): Promise<SubscriptionPlan> => {
    const response = await apiClient.get<{ success: boolean; data: SubscriptionPlan }>(
      '/subscription/plan'
    );
    return response.data.data;
  },

  /**
   * Create a new Razorpay order for subscription payment
   */
  createOrder: async (planId?: number): Promise<RazorpayOrderResponse> => {
    const response = await apiClient.post<{ success: boolean; data: RazorpayOrderResponse }>(
      '/subscription/create-order',
      { planId }
    );
    return response.data.data;
  },

  /**
   * Verify Razorpay payment signature after checkout completion
   */
  verifyPayment: async (payload: VerifyPaymentRequest): Promise<Subscription> => {
    const response = await apiClient.post<{ success: boolean; data: Subscription }>(
      '/subscription/verify-payment',
      payload
    );
    return response.data.data;
  },

  /**
   * Cancel user subscription
   */
  cancelSubscription: async (): Promise<Subscription> => {
    const response = await apiClient.post<{ success: boolean; data: Subscription }>(
      '/subscription/cancel'
    );
    return response.data.data;
  },
};
