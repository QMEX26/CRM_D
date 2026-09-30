import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
  ActivityIndicator,
  Platform,
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
import { subscriptionApi } from '../../api/subscriptionApi';
import { Subscription, SubscriptionPlan, RazorpayOrderResponse } from '../../types/subscription';

export const SubscriptionScreen: React.FC = () => {
  const navigation = useNavigation();
  const insets = useSafeAreaInsets();
  const { user, isAdmin } = useAuth();

  const [loading, setLoading] = useState(true);
  const [subscribing, setSubscribing] = useState(false);
  const [subscription, setSubscription] = useState<Subscription | null>(null);
  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);

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
      // 1. Request backend to create Razorpay order
      const order: RazorpayOrderResponse = await subscriptionApi.createOrder();

      // 2. Initiate Payment Checkout Flow
      // If running in development or sandbox mode without native SDK attached, prompt simulated verification
      Alert.alert(
        'Razorpay Checkout',
        `Initiating payment of ₹${order.amount} for ${order.planName}.\n\nOrder ID: ${order.orderId}`,
        [
          {
            text: 'Cancel',
            style: 'cancel',
            onPress: () => setSubscribing(false),
          },
          {
            text: 'Simulate Success (Sandbox)',
            onPress: async () => {
              try {
                // In production, razorpay_signature is returned from the checkout modal
                const verifiedSub = await subscriptionApi.verifyPayment({
                  razorpayOrderId: order.orderId,
                  razorpayPaymentId: 'pay_' + Math.random().toString(36).substring(2, 12),
                  razorpaySignature: 'mock_sig_' + order.orderId,
                });
                setSubscription(verifiedSub);
                Alert.alert(
                  '🎉 Subscription Activated',
                  `Your ${order.planName} is now ACTIVE! Thank you for subscribing.`
                );
              } catch (verifyErr: any) {
                Alert.alert('Payment Verification Failed', verifyErr.message || 'Signature mismatch');
              } finally {
                setSubscribing(false);
                loadData();
              }
            },
          },
        ]
      );
    } catch (err: any) {
      Alert.alert('Order Creation Failed', err.message || 'Could not initiate subscription order.');
      setSubscribing(false);
    }
  };

  const isTrial = subscription?.status === 'FREE_TRIAL';
  const isActive = subscription?.status === 'ACTIVE';
  const isExpired = subscription?.status === 'EXPIRED';

  const planPrice = plan?.price ?? (isAdmin ? 299 : 99);
  const planName = plan?.name ?? (isAdmin ? 'ADMIN PLAN' : 'USER PLAN');

  return (
    <View style={styles.container}>
      <AmbientBackground />
      <SafeAreaView style={styles.safeArea} edges={['top']}>
        <MeqHeader
          title="Subscription & Plan"
          subtitle={isAdmin ? 'Administrator Portal' : 'Sales Agent Workspace'}
          showBack
          onBackPress={() => navigation.goBack()}
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
                        : 'alert-circle-outline'
                    }
                    size={22}
                    color="#FFFFFF"
                  />
                  <Text style={styles.statusTitle}>
                    {isActive
                      ? 'ACTIVE SUBSCRIPTION'
                      : isTrial
                      ? '7-DAY FREE TRIAL'
                      : isExpired
                      ? 'SUBSCRIPTION EXPIRED'
                      : 'NO ACTIVE PLAN'}
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
                  ? `Your subscription is active and renewed.`
                  : isTrial
                  ? `You have ${subscription?.daysRemaining ?? 7} day(s) remaining in your free trial.`
                  : isExpired
                  ? `Your trial period has ended. Subscribe below to continue.`
                  : `Get started with full cloud CRM access.`}
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
                  <Text style={styles.planTargetLabel}>
                    {isAdmin ? 'ENTERPRISE ADMIN' : 'SALES AGENT WORKSPACE'}
                  </Text>
                  <Text style={styles.planNameText}>
                    {isAdmin ? 'Admin Master Plan' : 'Agent Pro Plan'}
                  </Text>
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
                    ? 'Renew / Extend Subscription (₹' + planPrice + ')'
                    : 'Subscribe via Razorpay (₹' + planPrice + '/mo)'
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
      </SafeAreaView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
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
