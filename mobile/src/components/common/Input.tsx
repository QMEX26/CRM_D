import React, { useState } from 'react';
import {
  View,
  TextInput,
  Text,
  StyleSheet,
  TextInputProps,
  TouchableOpacity,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '../../theme/colors';
import { spacing } from '../../theme/spacing';

interface InputProps extends TextInputProps {
  label?: string;
  error?: string;
  leftIcon?: keyof typeof Ionicons.glyphMap;
  isPassword?: boolean;
}

export const Input: React.FC<InputProps> = ({
  label,
  error,
  leftIcon,
  isPassword,
  style,
  ...props
}) => {
  const [isFocused, setIsFocused] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const isMultiline = Boolean(props.multiline);

  return (
    <View style={styles.container}>
      {label && <Text style={styles.label}>{label}</Text>}
      <View
        style={[
          styles.inputWrapper,
          isMultiline && styles.multilineWrapper,
          isFocused && styles.focusedWrapper,
          error ? styles.errorWrapper : null,
        ]}
      >
        {leftIcon && (
          <Ionicons
            name={leftIcon}
            size={18}
            color={isFocused ? colors.primary : colors.textMuted}
            style={[styles.leftIcon, isMultiline && { marginTop: 4 }]}
          />
        )}
        <TextInput
          style={[styles.input, isMultiline && styles.multilineInput, style]}
          placeholderTextColor={colors.textMuted}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          secureTextEntry={isPassword && !showPassword}
          {...props}
        />
        {isPassword && (
          <TouchableOpacity
            onPress={() => setShowPassword(!showPassword)}
            style={styles.rightIcon}
          >
            <Ionicons
              name={showPassword ? 'eye-off-outline' : 'eye-outline'}
              size={18}
              color={colors.textMuted}
            />
          </TouchableOpacity>
        )}
      </View>
      {error ? <Text style={styles.errorText}>{error}</Text> : null}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginBottom: spacing.normal,
    width: '100%',
  },
  label: {
    color: colors.textSecondary,
    fontSize: 13,
    fontWeight: '500',
    marginBottom: spacing.xs,
  },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: spacing.borderRadius.input,
    paddingHorizontal: spacing.md,
    height: 48,
  },
  multilineWrapper: {
    height: undefined,
    minHeight: 96,
    alignItems: 'flex-start',
    paddingVertical: spacing.sm,
  },
  focusedWrapper: {
    borderColor: colors.primary,
    backgroundColor: '#FFFFFF',
  },
  errorWrapper: {
    borderColor: colors.danger,
    backgroundColor: colors.dangerLight,
  },
  leftIcon: {
    marginRight: spacing.sm,
  },
  rightIcon: {
    padding: spacing.xs,
  },
  input: {
    flex: 1,
    color: colors.textPrimary,
    fontSize: 14,
    height: '100%',
  },
  multilineInput: {
    height: undefined,
    minHeight: 80,
    textAlignVertical: 'top',
    paddingTop: 2,
    paddingBottom: 2,
  },
  errorText: {
    color: colors.danger,
    fontSize: 12,
    marginTop: 4,
  },
});
