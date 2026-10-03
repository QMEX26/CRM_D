import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '../../theme/colors';
import { spacing } from '../../theme/spacing';

export interface FreeTrialCardProps {
  isTrialActive?: boolean;
  trialDaysRemaining?: number;
  trialStartDate?: string;
  trialEndDate?: string;
  isExpired?: boolean;
}

export const FreeTrialCard: React.FC<FreeTrialCardProps> = ({
  isTrialActive = false,
  trialDaysRemaining = 0,
  trialStartDate,
  trialEndDate,
  isExpired = false,
}) => {
  const totalTrialDays = 7;
  // Calculate remaining days bounded between 0 and 7
  const remainingDays = isTrialActive
    ? Math.max(0, Math.min(trialDaysRemaining ?? 0, totalTrialDays))
    : 0;

  // 7-day visual blocks array: true for active remaining yellow days, false for used/empty days
  const blocks = Array.from({ length: totalTrialDays }, (_, index) => index < remainingDays);

  const formattedEndDate = trialEndDate
    ? new Date(trialEndDate).toLocaleDateString('en-GB', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
      })
    : null;

  return (
    <View style={[styles.cardContainer, isExpired && styles.cardContainerExpired]}>
      {/* Top Header Badge & Counter */}
      <View style={styles.headerRow}>
        <View style={[styles.badge, isExpired ? styles.badgeExpired : styles.badgeActive]}>
          <Ionicons
            name={isExpired ? 'alert-circle-outline' : 'sparkles'}
            size={13}
            color={isExpired ? '#EF4444' : '#CA8A04'}
          />
          <Text style={[styles.badgeText, isExpired ? styles.badgeTextExpired : styles.badgeTextActive]}>
            {isExpired ? 'FREE TRIAL EXPIRED' : 'FREE TRIAL'}
          </Text>
        </View>

        <Text style={styles.counterText}>
          {remainingDays} / {totalTrialDays} DAYS
        </Text>
      </View>

      {/* Main Remaining Headline */}
      <View style={styles.headlineRow}>
        <Text style={styles.headlineTitle}>
          {isExpired
            ? 'FREE TRIAL EXPIRED'
            : `${remainingDays} ${remainingDays === 1 ? 'DAY' : 'DAYS'} REMAINING`}
        </Text>
      </View>

      {/* 7-DAY BLOCK UI */}
      <View style={styles.blocksTrack}>
        {blocks.map((isActive, index) => (
          <View
            key={`trial-block-${index}`}
            style={[
              styles.dayBlock,
              isActive ? styles.dayBlockActive : styles.dayBlockInactive,
            ]}
          >
            {isActive && <View style={styles.blockInnerShine} />}
          </View>
        ))}
      </View>

      {/* Bottom Information Details */}
      <View style={styles.footerRow}>
        <Ionicons
          name={isExpired ? 'time-outline' : 'calendar-outline'}
          size={14}
          color={isExpired ? '#DC2626' : colors.textSecondary}
        />
        <Text style={[styles.footerText, isExpired && styles.footerTextExpired]}>
          {isExpired
            ? 'Your 7-day free trial has ended. Subscribe below to continue.'
            : formattedEndDate
            ? `Trial ends: ${formattedEndDate}`
            : `${remainingDays} days remaining in trial`}
        </Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  cardContainer: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: spacing.md + 2,
    marginBottom: spacing.md,
    borderWidth: 1.5,
    borderColor: '#FDE047',
    shadowColor: '#CA8A04',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 10,
    elevation: 3,
  },
  cardContainerExpired: {
    borderColor: '#FECACA',
    backgroundColor: '#FFFFFF',
    shadowColor: '#DC2626',
    shadowOpacity: 0.05,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 8,
  },
  badgeActive: {
    backgroundColor: '#FEF9C3',
    borderWidth: 1,
    borderColor: '#FDE047',
  },
  badgeExpired: {
    backgroundColor: '#FEE2E2',
    borderWidth: 1,
    borderColor: '#FECACA',
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 0.6,
  },
  badgeTextActive: {
    color: '#854D0E',
  },
  badgeTextExpired: {
    color: '#991B1B',
  },
  counterText: {
    fontSize: 12,
    fontWeight: '700',
    color: colors.textSecondary,
    letterSpacing: 0.4,
  },
  headlineRow: {
    marginBottom: spacing.sm + 2,
  },
  headlineTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: colors.textPrimary,
    letterSpacing: 0.3,
  },
  blocksTrack: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 6,
    marginVertical: spacing.xs,
  },
  dayBlock: {
    flex: 1,
    height: 18,
    borderRadius: 5,
  },
  dayBlockActive: {
    backgroundColor: '#EAB308',
    borderColor: '#CA8A04',
    borderWidth: 1,
    shadowColor: '#EAB308',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3,
    elevation: 2,
  },
  dayBlockInactive: {
    backgroundColor: '#E2E8F0',
    borderColor: '#CBD5E1',
    borderWidth: 1,
  },
  blockInnerShine: {
    position: 'absolute',
    top: 1,
    left: 2,
    right: 2,
    height: 4,
    backgroundColor: 'rgba(255, 255, 255, 0.4)',
    borderRadius: 2,
  },
  footerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginTop: spacing.sm + 2,
    paddingTop: spacing.xs + 2,
    borderTopWidth: 1,
    borderTopColor: '#F1F5F9',
  },
  footerText: {
    fontSize: 12,
    fontWeight: '600',
    color: colors.textSecondary,
  },
  footerTextExpired: {
    color: '#DC2626',
  },
});
