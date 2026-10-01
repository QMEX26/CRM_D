import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
  ActivityIndicator,
  BackHandler,
} from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { colors } from '../../theme/colors';
import { spacing } from '../../theme/spacing';
import { GradientView } from '../../components/common/GradientView';
import { MeqHeader } from '../../components/common/MeqHeader';
import { Button } from '../../components/common/Button';
import { AmbientBackground } from '../../components/common/AmbientBackground';
import { useAuth } from '../../context/AuthContext';
import { useSubscription } from '../../context/SubscriptionContext';
import { subscriptionApi } from '../../api/subscriptionApi';
import { Subscription, SubscriptionPlan, RazorpayOrderResponse } from '../../types/subscription';
import { RazorpayCheckoutModal, RazorpaySuccessPayload } from '../../components/subscription/RazorpayCheckoutModal';

interface SubscriptionScreenProps {
  isGateMode?: boolean;
}

export const SubscriptionScreen: React.FC<SubscriptionScreenProps> = ({ isGateMode = false }) => {
  const navigation = useNavigation();
  const insets = useSafeAreaInsets();
  const { user, isAdmin, logout } = useAuth();
  const { refreshSubscription } = useSubscription();

  const [loading, setLoading] = useState(true);
  const [subscribing, setSubscribing] = useState(false);
  const [subscription, setSubscription] = useState<Subscription | null>(null);
  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);

  // Real Razorpay Checkout modal state
  const [checkoutOrder, setCheckoutOrder] = useState<RazorpayOrderResponse | null>(null);
  const [checkoutVisible, setCheckoutVisible] = useState(false);

  // Trap Android hardware back button if user is in Gate Mode
  useEffect(() => {
    if (isGateMode) {
      const backHandler = BackHandler.addEventListener('hardwareBackPress', () => {
        return true;
      });
      return () => backHandler.remove();
    }
  }, [isGateMode]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [subData, planData] = await Promise.all([
        subscriptionApi.getMySubscription().catch(() => null),
        subscriptionApi.getMyPlan().catch(() => null),
      ]);
      if (subData) setSubscription(subData);
      if (planData) setPlan(planData);
    } catch (err: any) {
      console.warn('Failed to load subscription details:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSubscribe = async () => {
    setSubscribing(true);
    try {
      // 1. Request backend to create Razorpay order (Backend enforces server-side role pricing)
      const order: RazorpayOrderResponse = await subscriptionApi.createOrder();
      setCheckoutOrder(order);
      setCheckoutVisible(true);
    } catch (err: any) {
      Alert.alert('Order Creation Failed', err.message || 'Could not initiate subscription order.');
    } finally {
      setSubscribing(false);
    }
  };

  const handlePaymentSuccess = async (data: RazorpaySuccessPayload) => {
    setCheckoutVisible(false);
    setLoading(true);
    try {
      const verifiedSub = await subscriptionApi.verifyPayment({
        razorpayOrderId: data.razorpay_order_id,
        razorpayPaymentId: data.razorpay_payment_id,
        razorpaySignature: data.razorpay_signature,
      });
      setSubscription(verifiedSub);

      // Global subscription context update to immediately unblock the SubscriptionGate
      await refreshSubscription();

      Alert.alert(
        '🎉 Subscription Activated',
        `Your payment was successful! Your subscription is now ACTIVE.`
      );
    } catch (verifyErr: any) {
      Alert.alert('Payment Verification Failed', verifyErr.message || 'Signature mismatch');
    } finally {
      setLoading(false);
      loadData();
    }
  };

  const handlePaymentFailure = (error: any) => {
    setCheckoutVisible(false);
    console.warn('Razorpay checkout failed or cancelled:', error);
    Alert.alert(
      'Payment Incomplete',
      error?.description || error?.message || 'The payment process was closed or not completed.'
    );
  };

  const handleLogout = async () => {
    Alert.alert('Sign Out', 'Are you sure you want to sign out?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Sign Out',
        style: 'destructive',
        onPress: async () => {
          await logout();
        },
      },
    ]);
  };

  const isActive = subscription?.status === 'ACTIVE' && subscription?.isActive !== false;
  const isTrial = !isActive && subscription?.status === 'FREE_TRIAL' && (subscription?.isTrial || (subscription?.daysRemaining ?? 0) > 0);
  const isExpired = subscription?.status === 'EXPIRED' || (!isActive && !isTrial);

  const planPrice = plan?.price ?? (isAdmin ? 299 : 99);
  const planRoleLabel = isAdmin ? 'ENTERPRISE ADMIN' : 'SALES AGENT WORKSPACE';
  const planTitle = isAdmin ? 'Admin Master Plan' : 'Agent Pro Plan';

  return (
    <AmbientBackground>
      <SafeAreaView style={styles.safeArea} edges={['top']}>
        <MeqHeader
          title={isGateMode ? 'Subscription Required' : 'Subscription & Plan'}
          subtitle={isAdmin ? 'Administrator Portal' : 'Sales Agent Workspace'}
          onBack={isGateMode ? undefined : () => navigation.goBack()}
          rightElement={
            isGateMode ? (
              <TouchableOpacity
                onPress={handleLogout}
                style={styles.headerLogoutBtn}
                activeOpacity={0.7}
              >
                <Ionicons name="log-out-outline" size={18} color="#EF4444" />
                <Text style={styles.headerLogoutText}>Sign Out</Text>
              </TouchableOpacity>
            ) : undefined
          }
        />

        {loading ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color={colors.primary} />
            <Text style={styles.loadingText}>Loading subscription status...</Text>
          </View>
        ) : (
          <ScrollView
            contentContainerStyle={[
              styles.scrollContent,
              { paddingBottom: insets.bottom + spacing.xl },
            ]}
            showsVerticalScrollIndicator={false}
          >
            {/* Status Card */}
            <GradientView
              colors={
                isActive
                  ? ['#065F46', '#047857']
                  : isTrial
                  ? ['#1E3A8A', '#2563EB']
                  : ['#7F1D1D', '#991B1B']
              }
              style={styles.statusCard}
            >
              <View style={styles.statusHeader}>
                <View style={styles.statusBadgeRow}>
                  <Ionicons
                    name={
                      isActive
                        ? 'checkmark-circle'
                        : isTrial
                        ? 'timer-outline'
                        : 'lock-closed'
                    }
                    size={22}
                    color="#FFFFFF"
                  />
                  <Text style={styles.statusTitle}>
                    {isActive
                      ? 'ACTIVE SUBSCRIPTION'
                      : isTrial
                      ? '7-DAY FREE TRIAL'
                      : 'SUBSCRIPTION REQUIRED'}
                  </Text>
                </View>
                <View style={styles.roleChip}>
                  <Text style={styles.roleChipText}>
                    {isAdmin ? 'ADMIN' : 'AGENT'}
                  </Text>
                </View>
              </View>

              <Text style={styles.statusSubtitle}>
                {isActive
                  ? `Your subscription is active and in good standing.`
                  : isTrial
                  ? `You have ${subscription?.daysRemaining ?? 7} day(s) remaining in your free trial.`
                  : `Your free trial/subscription has ended. Subscribe below to continue using the CRM.`}
              </Text>

              {subscription?.currentPeriodEnd && isActive && (
                <Text style={styles.expiryNote}>
                  Renewal Date: {new Date(subscription.currentPeriodEnd).toLocaleDateString('en-IN', {
                    day: 'numeric',
                    month: 'short',
                    year: 'numeric',
                  })}
                </Text>
              )}
            </GradientView>

            {/* Plan Card */}
            <View style={styles.planCard}>
              <View style={styles.planHeader}>
                <View>
                  <Text style={styles.planTargetLabel}>{planRoleLabel}</Text>
                  <Text style={styles.planNameText}>{planTitle}</Text>
                </View>
                <View style={styles.priceTag}>
                  <Text style={styles.currencySymbol}>₹</Text>
                  <Text style={styles.priceAmount}>{planPrice}</Text>
                  <Text style={styles.pricePeriod}> / mo</Text>
                </View>
              </View>

              <View style={styles.divider} />

              <Text style={styles.featureTitle}>What is included:</Text>

              <View style={styles.featureList}>
                <View style={styles.featureItem}>
                  <Ionicons name="checkmark-circle-outline" size={18} color={colors.primary} />
                  <Text style={styles.featureText}>Full CRM Cloud Database Sync (MySQL/Cloud)</Text>
                </View>
                <View style={styles.featureItem}>
                  <Ionicons name="checkmark-circle-outline" size={18} color={colors.primary} />
                  <Text style={styles.featureText}>Smart Call Tracking & Automatic Classification</Text>
                </View>
                <View style={styles.featureItem}>
                  <Ionicons name="checkmark-circle-outline" size={18} color={colors.primary} />
                  <Text style={styles.featureText}>Real-time Lead Management & Notifications</Text>
                </View>
                <View style={styles.featureItem}>
                  <Ionicons name="checkmark-circle-outline" size={18} color={colors.primary} />
                  <Text style={styles.featureText}>Attendance & Shift Scheduling Control</Text>
                </View>
                {isAdmin && (
                  <View style={styles.featureItem}>
                    <Ionicons name="checkmark-circle-outline" size={18} color={colors.primary} />
                    <Text style={styles.featureText}>Admin Master Control & Google Sheets Integration</Text>
                  </View>
                )}
                <View style={styles.featureItem}>
                  <Ionicons name="checkmark-circle-outline" size={18} color={colors.primary} />
                  <Text style={styles.featureText}>Secure 256-bit Encrypted Cloud Infrastructure</Text>
                </View>
              </View>

              <Button
                title={
                  isActive
                    ? `Renew / Extend Subscription (₹${planPrice}/mo)`
                    : `Subscribe via Razorpay (₹${planPrice}/mo)`
                }
                onPress={handleSubscribe}
                loading={subscribing}
                style={styles.subscribeBtn}
              />

              <View style={styles.razorpayBadgeRow}>
                <Ionicons name="shield-checkmark" size={14} color={colors.textSecondary} />
                <Text style={styles.razorpayBadgeText}>
                  Secured by Razorpay • Instant Verification
                </Text>
              </View>
            </View>

            {/* User Details Footer */}
            <View style={styles.accountCard}>
              <Text style={styles.accountCardTitle}>Account Information</Text>
              <View style={styles.accountRow}>
                <Text style={styles.accountLabel}>Name:</Text>
                <Text style={styles.accountValue}>{user?.name}</Text>
              </View>
              <View style={styles.accountRow}>
                <Text style={styles.accountLabel}>Email:</Text>
                <Text style={styles.accountValue}>{user?.email}</Text>
              </View>
              <View style={styles.accountRow}>
                <Text style={styles.accountLabel}>Role:</Text>
                <Text style={styles.accountValue}>{user?.role}</Text>
              </View>
            </View>
          </ScrollView>
        )}

        <RazorpayCheckoutModal
          visible={checkoutVisible}
          order={checkoutOrder}
          onSuccess={handlePaymentSuccess}
          onFailure={handlePaymentFailure}
          onClose={() => setCheckoutVisible(false)}
        />
      </SafeAreaView>
    </AmbientBackground>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    color: colors.textSecondary,
    marginTop: spacing.md,
    fontSize: 14,
  },
  scrollContent: {
    paddingHorizontal: spacing.md,
    paddingTop: spacing.sm,
  },
  headerLogoutBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: 'rgba(239, 68, 68, 0.1)',
    paddingVertical: 5,
    paddingHorizontal: 10,
    borderRadius: 8,
  },
  headerLogoutText: {
    color: '#EF4444',
    fontSize: 12,
    fontWeight: '700',
  },
  statusCard: {
    borderRadius: 16,
    padding: spacing.lg,
    marginBottom: spacing.md,
  },
  statusHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.xs,
  },
  statusBadgeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  statusTitle: {
    color: '#FFFFFF',
    fontWeight: '700',
    fontSize: 14,
    letterSpacing: 0.5,
  },
  roleChip: {
    backgroundColor: 'rgba(255,255,255,0.2)',
    paddingHorizontal: spacing.sm,
    paddingVertical: 2,
    borderRadius: 10,
  },
  roleChipText: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: '700',
  },
  statusSubtitle: {
    color: '#E2E8F0',
    fontSize: 13,
    marginTop: spacing.xs,
    lineHeight: 18,
  },
  expiryNote: {
    color: '#93C5FD',
    fontSize: 12,
    fontWeight: '600',
    marginTop: spacing.xs,
  },
  planCard: {
    backgroundColor: colors.surface,
    borderRadius: 16,
    padding: spacing.lg,
    borderWidth: 1,
    borderColor: colors.border,
    marginBottom: spacing.md,
  },
  planHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  planTargetLabel: {
    color: colors.primary,
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
  planNameText: {
    color: colors.textPrimary,
    fontSize: 18,
    fontWeight: '700',
    marginTop: 2,
  },
  priceTag: {
    flexDirection: 'row',
    alignItems: 'baseline',
  },
  currencySymbol: {
    color: colors.textPrimary,
    fontSize: 18,
    fontWeight: '600',
  },
  priceAmount: {
    color: colors.textPrimary,
    fontSize: 28,
    fontWeight: '800',
  },
  pricePeriod: {
    color: colors.textSecondary,
    fontSize: 13,
  },
  divider: {
    height: 1,
    backgroundColor: colors.border,
    marginVertical: spacing.md,
  },
  featureTitle: {
    color: colors.textPrimary,
    fontSize: 14,
    fontWeight: '600',
    marginBottom: spacing.sm,
  },
  featureList: {
    gap: spacing.sm,
    marginBottom: spacing.lg,
  },
  featureItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  featureText: {
    color: colors.textSecondary,
    fontSize: 13,
    flex: 1,
  },
  subscribeBtn: {
    marginTop: spacing.xs,
  },
  razorpayBadgeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    marginTop: spacing.sm,
  },
  razorpayBadgeText: {
    color: colors.textSecondary,
    fontSize: 11,
  },
  accountCard: {
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
  },
  accountCardTitle: {
    color: colors.textPrimary,
    fontSize: 13,
    fontWeight: '600',
    marginBottom: spacing.sm,
  },
  accountRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 4,
  },
  accountLabel: {
    color: colors.textSecondary,
    fontSize: 12,
  },
  accountValue: {
    color: colors.textPrimary,
    fontSize: 12,
    fontWeight: '500',
  },
});
