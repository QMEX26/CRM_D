import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Alert,
} from 'react-native';
import { colors } from '../../theme/colors';
import { spacing } from '../../theme/spacing';
import { Input } from '../common/Input';
import { FormModal } from '../common/FormModal';
import { callApi } from '../../api/callApi';

interface LogCallModalProps {
  visible: boolean;
  leadId: number;
  leadName: string;
  leadPhone?: string;
  onClose: () => void;
  onCallLogged: () => void;
}

const CALL_STATUSES = [
  'CONNECTED',
  'NO_ANSWER',
  'BUSY',
  'FAILED',
  'WRONG_NUMBER',
];

const OUTCOMES = [
  'INTERESTED',
  'FOLLOW_UP',
  'NOT_INTERESTED',
  'WRONG_NUMBER',
  'JUNK',
];

export const LogCallModal: React.FC<LogCallModalProps> = ({
  visible,
  leadId,
  leadName,
  leadPhone,
  onClose,
  onCallLogged,
}) => {
  const [durationMinutes, setDurationMinutes] = useState('1');
  const [status, setStatus] = useState('CONNECTED');
  const [outcome, setOutcome] = useState('INTERESTED');
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSave = async () => {
    const mins = parseFloat(durationMinutes) || 1;
    const durationSeconds = Math.round(mins * 60);

    setSubmitting(true);
    try {
      await callApi.logCall({
        leadId,
        durationSeconds,
        callStatus: status,
        businessOutcome: outcome,
        notes: notes.trim() || undefined,
      });

      Alert.alert('Success', 'Call logged successfully.');
      onCallLogged();
      onClose();
    } catch (err: any) {
      Alert.alert('Error', err.message || 'Failed to log call.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <FormModal
      visible={visible}
      onClose={onClose}
      title="Log Call Record"
      subtitle={`${leadName}${leadPhone ? ` (${leadPhone})` : ''}`}
      onSave={handleSave}
      saveTitle="Save"
      saveLoading={submitting}
      saveVariant="primary"
      heightPercent={0.84}
      maxHeightPixels={600}
    >
      {/* Duration Input */}
      <Input
        label="Call Duration (Minutes)"
        placeholder="e.g. 2"
        value={durationMinutes}
        onChangeText={setDurationMinutes}
        keyboardType="numeric"
        leftIcon="timer-outline"
      />

      {/* Call Status Selector */}
      <Text style={styles.sectionLabel}>Call Status</Text>
      <View style={styles.chipRow}>
        {CALL_STATUSES.map((st) => {
          const selected = status === st;
          return (
            <TouchableOpacity
              key={st}
              style={[styles.chip, selected && styles.chipActive]}
              onPress={() => setStatus(st)}
            >
              <Text style={[styles.chipText, selected && styles.chipTextActive]}>
                {st.replace('_', ' ')}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* Business Outcome Selector */}
      <Text style={styles.sectionLabel}>Business Outcome</Text>
      <View style={styles.chipRow}>
        {OUTCOMES.map((oc) => {
          const selected = outcome === oc;
          return (
            <TouchableOpacity
              key={oc}
              style={[styles.chip, selected && styles.chipActive]}
              onPress={() => setOutcome(oc)}
            >
              <Text style={[styles.chipText, selected && styles.chipTextActive]}>
                {oc.replace('_', ' ')}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* Call Notes */}
      <Input
        label="Call Notes & Discussion Summary"
        placeholder="Key points discussed during call..."
        value={notes}
        onChangeText={setNotes}
        multiline
        numberOfLines={3}
        style={styles.textArea}
      />
    </FormModal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(17, 24, 39, 0.45)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: colors.surfaceElevated,
    borderTopLeftRadius: spacing.borderRadius.xl,
    borderTopRightRadius: spacing.borderRadius.xl,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.lg,
    paddingBottom: 0,
    width: '100%',
    flexDirection: 'column',
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: colors.border,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    paddingBottom: spacing.sm,
    flexShrink: 0,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.textPrimary,
  },
  modalSubtitle: {
    fontSize: 13,
    color: colors.textSecondary,
    marginTop: 2,
  },
  closeBtn: {
    padding: spacing.xs,
  },
  scrollBody: {
    width: '100%',
  },
  scrollBodyContent: {
    paddingBottom: spacing.sm,
  },
  sectionLabel: {
    color: colors.textSecondary,
    fontSize: 13,
    fontWeight: '500',
    marginBottom: spacing.xs + 2,
    marginTop: spacing.xs,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.xs + 2,
    marginBottom: spacing.md,
  },
  chip: {
    paddingHorizontal: spacing.sm + 4,
    paddingVertical: spacing.xs + 3,
    borderRadius: spacing.borderRadius.sm,
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
  },
  chipActive: {
    backgroundColor: colors.primaryLight,
    borderColor: colors.primary,
  },
  chipText: {
    fontSize: 12,
    color: colors.textSecondary,
    fontWeight: '500',
  },
  chipTextActive: {
    color: colors.primary,
    fontWeight: '700',
  },
  textArea: {
    height: 70,
    textAlignVertical: 'top',
  },
  footer: {
    flexDirection: 'row',
    gap: spacing.md,
    paddingTop: spacing.md,
    marginTop: spacing.sm,
    flexShrink: 0,
  },
  footerBtn: {
    flex: 1,
  },
});
