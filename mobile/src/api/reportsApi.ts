import { apiClient, getApiBaseUrl } from './client';
import { ApiResponse } from '../types';

export interface AdminAnalyticsDashboardData {
  kpis: {
    totalLeads: number;
    totalCalls: number;
    connectedCalls: number;
    missedCalls: number;
    totalTalkTimeSeconds: number;
    avgTalkTimeSeconds: number;
    conversions: number;
    conversionRate: number;
    pendingFollowUps: number;
    overdueFollowUps: number;
  };
  callPerformance: {
    connectedCount: number;
    missedCount: number;
    inboundCount: number;
    outboundCount: number;
    classificationBreakdown: Record<string, number>;
    timelineData: Array<{ label: string; calls: number; connected: number }>;
  };
  leadPerformance: {
    stageBreakdown: Record<string, number>;
    outcomeBreakdown: Record<string, number>;
    conversionRate: number;
  };
  projectPerformance: Array<{
    projectId: number;
    projectName: string;
    totalLeads: number;
    totalCalls: number;
    conversions: number;
    conversionRate: number;
  }>;
  userActivity: Array<{
    userId: number;
    userName: string;
    role: string;
    assignedLeads: number;
    totalCalls: number;
    connectedCalls: number;
    talkTimeSeconds: number;
    conversions: number;
    followUpsHandled: number;
  }>;
  followUpReport: {
    pendingCount: number;
    completedCount: number;
    overdueCount: number;
  };
}

export const reportsApi = {
  getAdminAnalyticsDashboard: async (params?: {
    projectId?: number;
    userId?: number;
    leadStatus?: string;
    callStatus?: string;
    callDirection?: string;
    start?: string;
    end?: string;
  }): Promise<AdminAnalyticsDashboardData> => {
    const res = await apiClient.get<ApiResponse<AdminAnalyticsDashboardData>>('/reports/dashboard', { params });
    return res.data.data;
  },

  exportReportCsv: async (params: {
    type: 'LEADS' | 'CALLS' | 'CONVERSIONS' | 'FOLLOWUPS';
    projectId?: number;
    userId?: number;
    leadStatus?: string;
    callStatus?: string;
    callDirection?: string;
    start?: string;
    end?: string;
  }): Promise<string> => {
    const res = await apiClient.get('/reports/export', {
      params,
      responseType: 'text',
    });
    return res.data;
  },

  getLeadReport: async (params?: { projectId?: number; startDate?: string; endDate?: string }) => {
    const res = await apiClient.get<ApiResponse<any>>('/reports/leads', { params });
    return res.data.data;
  },

  getCallReport: async (params?: { userId?: number; startDate?: string; endDate?: string }) => {
    const res = await apiClient.get<ApiResponse<any>>('/reports/calls', { params });
    return res.data.data;
  },

  getEmployeeReport: async (params?: { startDate?: string; endDate?: string }) => {
    const res = await apiClient.get<ApiResponse<any>>('/reports/employees', { params });
    return res.data.data;
  },

  getProjectReport: async () => {
    const res = await apiClient.get<ApiResponse<any>>('/reports/projects');
    return res.data.data;
  },

  getSalesReport: async (params?: { startDate?: string; endDate?: string }) => {
    const res = await apiClient.get<ApiResponse<any>>('/reports/sales', { params });
    return res.data.data;
  },
};
