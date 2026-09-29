import React from 'react';
import { ViewStyle, TextStyle, StyleProp } from 'react-native';
import { StatusBadge } from './StatusBadge';

interface BadgeProps {
  label: string;
  status?: string;
  variant?: 'success' | 'warning' | 'danger' | 'info' | 'primary' | 'neutral';
  style?: StyleProp<ViewStyle>;
  textStyle?: StyleProp<TextStyle>;
  showDot?: boolean;
}

export const Badge: React.FC<BadgeProps> = (props) => {
  return <StatusBadge {...props} />;
};
