import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { subscriptionApi } from '../api/subscriptionApi';
import { Subscription, SubscriptionPlan } from '../types/subscription';
import { useAuth } from './AuthContext';

interface SubscriptionContextType {
  subscription: Subscription | null;
  plan: SubscriptionPlan | null;
  isLoading: boolean;
  isTrial: boolean;
  isActive: boolean;
  isExpired: boolean;
  daysRemaining: number;
  refreshSubscription: () => Promise<void>;
}

const SubscriptionContext = createContext<SubscriptionContextType | undefined>(undefined);

export const SubscriptionProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [subscription, setSubscription] = useState<Subscription | null>(null);
  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const refreshSubscription = async () => {
    if (!isAuthenticated) return;
    try {
      setIsLoading(true);
      const [subData, planData] = await Promise.all([
        subscriptionApi.getMySubscription().catch(() => null),
        subscriptionApi.getMyPlan().catch(() => null),
      ]);
      if (subData) {
        setSubscription(subData);
      }
      if (planData) {
        setPlan(planData);
      }
    } catch (e) {
      console.warn('[SubscriptionContext] Error loading subscription:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      refreshSubscription();
    } else {
      setSubscription(null);
      setPlan(null);
    }
  }, [isAuthenticated]);

  const isTrial = subscription?.status === 'FREE_TRIAL';
  const isActive = subscription?.status === 'ACTIVE';
  const isExpired = subscription?.status === 'EXPIRED';
  const daysRemaining = subscription?.daysRemaining ?? 0;

  return (
    <SubscriptionContext.Provider
      value={{
        subscription,
        plan,
        isLoading,
        isTrial,
        isActive,
        isExpired,
        daysRemaining,
        refreshSubscription,
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
