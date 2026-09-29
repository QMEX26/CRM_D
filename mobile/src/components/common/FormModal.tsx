import React from 'react';
import {
  Modal,
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  useWindowDimensions,
  ViewStyle,
  StyleProp,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '../../theme/colors';
import { spacing } from '../../theme/spacing';
import { Button } from './Button';

export interface FormModalProps {
  visible: boolean;
  onClose: () => void;
  title: string;
  subtitle?: string;
  headerComponent?: React.ReactNode;

  onSave?: () => void;
  saveTitle?: string;
  saveLoading?: boolean;
  saveDisabled?: boolean;
  saveVariant?: 'primary' | 'success' | 'danger';
  saveIcon?: React.ReactNode;
  cancelTitle?: string;
  showCancel?: boolean;
  customFooter?: React.ReactNode;

  heightPercent?: number;
  maxHeightPixels?: number;
  contentContainerStyle?: StyleProp<ViewStyle>;
  scrollRef?: React.RefObject<ScrollView | null>;
  children: React.ReactNode;
}

export const FormModal: React.FC<FormModalProps> = ({
  visible,
  onClose,
  title,
  subtitle,
  headerComponent,
  onSave,
  saveTitle = 'Save',
  saveLoading = false,
  saveDisabled = false,
  saveVariant = 'primary',
  saveIcon = <Ionicons name="checkmark-circle" size={18} color="#ffffff" />,
  cancelTitle = 'Cancel',
  showCancel = true,
  customFooter,
  heightPercent = 0.82,
  maxHeightPixels = 580,
  contentContainerStyle,
  scrollRef,
  children,
}) => {
  const { height: screenHeight } = useWindowDimensions();
  const insets = useSafeAreaInsets();

  // Calculate real finite height per the architectural flex rule:
  // Container must have a finite height so Body (flex: 1, minHeight: 0)
  // absorbs scroll content and Footer (flexShrink: 0) is NEVER clipped or pushed out.
  const modalHeight = Math.min(
    Math.max(screenHeight * heightPercent, 380),
    maxHeightPixels,
    screenHeight - (insets.top + insets.bottom + 20)
  );

  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={onClose}
    >
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={styles.overlay}
      >
        <TouchableOpacity
          style={styles.backdropTouchable}
          activeOpacity={1}
          onPress={onClose}
        />

        <View
          style={[
            styles.modalContainer,
            { height: modalHeight },
          ]}
          onStartShouldSetResponder={() => true}
        >
          {/* 1. FIXED HEADER: flexShrink: 0 */}
          <View style={styles.header}>
            <View style={styles.headerTitleRow}>
              <View style={{ flex: 1 }}>
                <Text style={styles.title} numberOfLines={1}>{title}</Text>
                {subtitle ? (
                  <Text style={styles.subtitle} numberOfLines={1}>{subtitle}</Text>
                ) : null}
              </View>
              <TouchableOpacity
                onPress={onClose}
                hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
                style={styles.closeBtn}
              >
                <Ionicons name="close" size={22} color={colors.textSecondary} />
              </TouchableOpacity>
            </View>
            {headerComponent}
          </View>

          {/* 2. BODY: flex: 1, minHeight: 0 absorbs all scrollable content */}
          <View style={styles.body}>
            <ScrollView
              ref={scrollRef as any}
              style={styles.scrollView}
              contentContainerStyle={[styles.scrollContent, contentContainerStyle]}
              keyboardShouldPersistTaps="handled"
              showsVerticalScrollIndicator={true}
            >
              {children}
            </ScrollView>
          </View>

          {/* 3. FIXED FOOTER: flexShrink: 0, minHeight: 70, ALWAYS visible at modal bottom */}
          {customFooter ? (
            <View style={styles.footer}>
              {customFooter}
            </View>
          ) : (
            <View style={styles.footer}>
              {showCancel && (
                <View style={styles.footerBtnWrapper}>
                  <Button
                    title={cancelTitle}
                    variant="outline"
                    onPress={onClose}
                    style={{ width: '100%' }}
                  />
                </View>
              )}
              {onSave && (
                <View style={styles.footerBtnWrapper}>
                  <Button
                    title={saveTitle}
                    variant={saveVariant}
                    onPress={onSave}
                    loading={saveLoading}
                    disabled={saveDisabled}
                    icon={saveIcon}
                    style={{ width: '100%' }}
                  />
                </View>
              )}
            </View>
          )}
        </View>
      </KeyboardAvoidingView>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(17, 24, 39, 0.55)',
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md,
  },
  backdropTouchable: {
    ...StyleSheet.absoluteFill,
  },
  modalContainer: {
    width: '100%',
    maxWidth: 440,
    backgroundColor: colors.surface,
    borderRadius: spacing.borderRadius.lg,
    flexDirection: 'column',
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: colors.border,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.2,
    shadowRadius: 20,
    elevation: 10,
  },
  header: {
    flexShrink: 0,
    paddingHorizontal: spacing.md,
    paddingTop: spacing.md,
    paddingBottom: spacing.xs,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    backgroundColor: colors.surface,
  },
  headerTitleRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.xs,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.textPrimary,
  },
  subtitle: {
    fontSize: 12,
    color: colors.textSecondary,
    marginTop: 2,
  },
  closeBtn: {
    padding: 4,
    marginLeft: spacing.sm,
  },
  body: {
    flex: 1,
    minHeight: 0,
    width: '100%',
  },
  scrollView: {
    flex: 1,
    width: '100%',
  },
  scrollContent: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    paddingBottom: spacing.lg,
  },
  footer: {
    flexShrink: 0,
    minHeight: 70,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.sm,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderTopWidth: 1.5,
    borderTopColor: colors.border,
    backgroundColor: '#F8FAFC',
  },
  footerBtnWrapper: {
    flex: 1,
  },
});
