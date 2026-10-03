import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { subscriptionApi } from '../api/subscriptionApi';
import { Subscription, SubscriptionPlan } from '../types/subscription';
import { useAuth } from './AuthContext';

interface SubscriptionContextType {
  subscription: Subscription | null;
  plan: SubscriptionPlan | null;
  isLoading: boolean;
  error: string | null;
  isTrialActive: boolean;
  isSubscriptionActive: boolean;
  isSubscriptionExpired: boolean;
  isSubscriptionCancelled: boolean;
  isAccessGranted: boolean;
  requiresSubscription: boolean;
  daysRemaining: number;
  refreshSubscription: () => Promise<Subscription | null>;
  clearSubscription: () => void;
}

const SubscriptionContext = createContext<SubscriptionContextType | undefined>(undefined);

export const SubscriptionProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [subscription, setSubscription] = useState<Subscription | null>(null);
  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const clearSubscription = useCallback(() => {
    setSubscription(null);
    setPlan(null);
    setError(null);
    setIsLoading(false);
  }, []);

  const refreshSubscription = useCallback(async (): Promise<Subscription | null> => {
    if (!isAuthenticated) {
      clearSubscription();
      return null;
    }
    try {
      setIsLoading(true);
      setError(null);
      const [subData, planData] = await Promise.all([
        subscriptionApi.getMySubscription(),
        subscriptionApi.getMyPlan().catch(() => null),
      ]);
      setSubscription(subData);
      if (planData) {
        setPlan(planData);
      }
      return subData;
    } catch (err: any) {
      console.warn('[SubscriptionContext] Error fetching subscription:', err);
      const errMsg =
        err?.message ||
        'Unable to verify subscription status. Please check your internet connection.';
      setError(errMsg);
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, clearSubscription]);

  useEffect(() => {
    if (isAuthenticated) {
      refreshSubscription();
    } else {
      clearSubscription();
    }
  }, [isAuthenticated, refreshSubscription, clearSubscription]);

  // Subscription state logic
  const isTrialActive =
    subscription?.status === 'FREE_TRIAL' &&
    (subscription.isTrial || subscription.daysRemaining > 0);

  const isSubscriptionActive =
    subscription?.status === 'ACTIVE' && subscription.isActive;

  const isSubscriptionExpired =
    subscription?.status === 'EXPIRED' ||
    (subscription?.status === 'FREE_TRIAL' &&
      subscription.daysRemaining <= 0 &&
      !subscription.isTrial);

  const isSubscriptionCancelled = subscription?.status === 'CANCELLED';

  // Access is granted if user is in an active free trial OR has an active paid subscription
  const isAccessGranted = isTrialActive || isSubscriptionActive;

  // Subscription is required when user has been evaluated and does NOT have granted access
  const requiresSubscription =
    !isLoading && !error && subscription !== null && !isAccessGranted;

  const daysRemaining = subscription?.daysRemaining ?? 0;

  return (
    <SubscriptionContext.Provider
      value={{
        subscription,
        plan,
        isLoading,
        error,
        isTrialActive,
        isSubscriptionActive,
        isSubscriptionExpired,
        isSubscriptionCancelled,
        isAccessGranted,
        requiresSubscription,
        daysRemaining,
        refreshSubscription,
        clearSubscription,
      }}
    >
      {children}
    </SubscriptionContext.Provider>
  );
};

export const useSubscription = () => {
  const context = useContext(SubscriptionContext);
  if (!context) {
    throw new Error('useSubscription must be used within a SubscriptionProvider');
  }
  return context;
};
