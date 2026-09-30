import React, { useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
  BackHandler,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '../../theme/colors';
import { spacing } from '../../theme/spacing';
import { useAuth } from '../../context/AuthContext';
import { useSubscription } from '../../context/SubscriptionContext';
import { SubscriptionScreen } from '../../screens/settings/SubscriptionScreen';
import { LoadingState } from '../common/LoadingState';
import { Button } from '../common/Button';

interface SubscriptionGateProps {
  children: React.ReactNode;
}

export const SubscriptionGate: React.FC<SubscriptionGateProps> = ({ children }) => {
  const { isAuthenticated, isLoading: authLoading, logout } = useAuth();
  const {
    subscription,
    isLoading: subLoading,
    error: subError,
    isAccessGranted,
    requiresSubscription,
    refreshSubscription,
  } = useSubscription();

  // Trap Android hardware back button when user is blocked on the gate
  useEffect(() => {
    if (requiresSubscription || (subError && !subscription)) {
      const backHandler = BackHandler.addEventListener('hardwareBackPress', () => {
        // Prevent bypassing subscription gate via Android hardware back button
        return true;
      });
      return () => backHandler.remove();
    }
  }, [requiresSubscription, subError, subscription]);

  // 1. If authentication or initial subscription check is in progress
  if (authLoading || (subLoading && !subscription && !subError)) {
    return <LoadingState message="Verifying subscription status..." fullScreen />;
  }

  // 2. If user is not authenticated, let root navigation handle login
  if (!isAuthenticated) {
    return null;
  }

  // 3. If there is a network error and we have no valid cached subscription
  if (subError && !subscription) {
    return (
      <View style={styles.networkErrorContainer}>
        <View style={styles.networkErrorCard}>
          <View style={styles.errorIconCircle}>
            <Ionicons name="cloud-offline-outline" size={36} color="#DC2626" />
          </View>
          <Text style={styles.networkErrorTitle}>Unable to Verify Subscription</Text>
          <Text style={styles.networkErrorSubtitle}>
            We could not reach the server to verify your subscription. Please check your internet
            connection and try again.
          </Text>

          <Button
            title="Retry Connection"
            onPress={refreshSubscription}
            loading={subLoading}
            style={styles.retryButton}
          />

          <TouchableOpacity
            style={styles.logoutOption}
            onPress={logout}
            activeOpacity={0.7}
          >
            <Ionicons name="log-out-outline" size={16} color="#64748B" />
            <Text style={styles.logoutOptionText}>Sign Out</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  // 4. If subscription is expired, cancelled, or missing valid access
  if (requiresSubscription || !isAccessGranted) {
    return <SubscriptionScreen isGateMode={true} />;
  }

  // 5. Access Granted (Free Trial Active or Paid Subscription Active)
  return <>{children}</>;
};

const styles = StyleSheet.create({
  networkErrorContainer: {
    flex: 1,
    backgroundColor: colors.background,
    justifyContent: 'center',
    alignItems: 'center',
    padding: spacing.lg,
  },
  networkErrorCard: {
    backgroundColor: colors.surface,
    borderRadius: 20,
    padding: spacing.xl,
    alignItems: 'center',
    width: '100%',
    maxWidth: 380,
    borderWidth: 1,
    borderColor: colors.border,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 12,
    elevation: 4,
  },
  errorIconCircle: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: '#FEE2E2',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: spacing.md,
  },
  networkErrorTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.textPrimary,
    textAlign: 'center',
    marginBottom: spacing.xs,
  },
  networkErrorSubtitle: {
    fontSize: 13,
    color: colors.textSecondary,
    textAlign: 'center',
    lineHeight: 19,
    marginBottom: spacing.lg,
  },
  retryButton: {
    width: '100%',
    marginBottom: spacing.md,
  },
  logoutOption: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingVertical: spacing.xs,
    paddingHorizontal: spacing.sm,
  },
  logoutOptionText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748B',
  },
});
