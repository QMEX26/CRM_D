import React from 'react';
import { View, StyleSheet, Platform } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '../theme/colors';
import { useAuth } from '../context/AuthContext';
import { HomeScreen } from '../screens/home/HomeScreen';
import { AdminDashboardScreen } from '../screens/admin/AdminDashboardScreen';
import { DialScreen } from '../screens/dial/DialScreen';
import { ProjectsScreen } from '../screens/leads/ProjectsScreen';
import { LeadsListScreen } from '../screens/leads/LeadsListScreen';
import { FollowUpsScreen } from '../screens/followups/FollowUpsScreen';
import { ReportsScreen } from '../screens/admin/ReportsScreen';
import { AdminHubScreen } from '../screens/admin/AdminHubScreen';
import { AnalyticsScreen } from '../screens/analytics/AnalyticsScreen';
import { SettingsScreen } from '../screens/settings/SettingsScreen';
import { LoadingState } from '../components/common/LoadingState';
import { FloatingTabBar } from '../components/common/FloatingTabBar';
import { MainTabParamList } from '../types';

const Tab = createBottomTabNavigator<MainTabParamList>();
const LeadsStack = createNativeStackNavigator();

const LeadsNavigator = () => {
  return (
    <LeadsStack.Navigator screenOptions={{ headerShown: false }}>
      <LeadsStack.Screen name="ProjectsList" component={ProjectsScreen} />
      <LeadsStack.Screen name="LeadsList" component={LeadsListScreen} />
    </LeadsStack.Navigator>
  );
};

export const TabNavigator: React.FC = () => {
  const { isAdmin, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingState message="Loading workspace..." fullScreen />;
  }

  return (
    <Tab.Navigator
      tabBar={(props) => <FloatingTabBar {...props} />}
      screenOptions={{
        headerShown: false,
      }}
    >
      {isAdmin ? (
        <>
          <Tab.Screen
            name="AdminDashboard"
            component={AdminDashboardScreen}
            options={{ tabBarLabel: 'Dashboard' }}
          />
          <Tab.Screen
            name="Leads"
            component={LeadsNavigator}
            options={{ tabBarLabel: 'Leads' }}
          />
          <Tab.Screen
            name="Dial"
            component={DialScreen}
            options={{ tabBarLabel: 'Dial' }}
          />
          <Tab.Screen
            name="AdminHub"
            component={AdminHubScreen}
            options={{ tabBarLabel: 'Admin' }}
          />
        </>
      ) : (
        <>
          <Tab.Screen
            name="Home"
            component={HomeScreen}
            options={{ tabBarLabel: 'Home' }}
          />
          <Tab.Screen
            name="Dial"
            component={DialScreen}
            options={{ tabBarLabel: 'Dial' }}
          />
          <Tab.Screen
            name="Analytics"
            component={AnalyticsScreen}
            options={{ tabBarLabel: 'Analytics' }}
          />
          <Tab.Screen
            name="Leads"
            component={LeadsNavigator}
            options={{ tabBarLabel: 'Leads' }}
          />
          <Tab.Screen
            name="Settings"
            component={SettingsScreen}
            options={{ tabBarLabel: 'Settings' }}
          />
        </>
      )}
    </Tab.Navigator>
  );
};

const styles = StyleSheet.create({
  tabBar: {
    backgroundColor: colors.surface,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    height: Platform.OS === 'ios' ? 88 : 62,
    paddingBottom: Platform.OS === 'ios' ? 28 : 6,
    paddingTop: 6,
    elevation: 4,
    shadowColor: '#111827',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.04,
    shadowRadius: 6,
  },
  tabBarLabel: {
    fontSize: 11,
    fontWeight: '600',
    marginTop: 2,
  },
  iconContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 44,
    height: 28,
    borderRadius: 14,
  },
  iconContainerActive: {
    backgroundColor: colors.primaryLight,
  },
});
