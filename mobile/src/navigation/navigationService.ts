import { createNavigationContainerRef } from '@react-navigation/native';
import { RootStackParamList } from '../types';

export const navigationRef = createNavigationContainerRef<RootStackParamList>();

/**
 * Resets navigation state to the Login screen.
 * Can be invoked from anywhere, including AuthContext, HTTP interceptors, etc.
 */
export function resetToLogin() {
  if (navigationRef.isReady()) {
    navigationRef.reset({
      index: 0,
      routes: [{ name: 'Login' }],
    });
  }
}

/**
 * Resets navigation state to the Main workspace screen.
 */
export function resetToMain() {
  if (navigationRef.isReady()) {
    navigationRef.reset({
      index: 0,
      routes: [{ name: 'Main' }],
    });
  }
}
