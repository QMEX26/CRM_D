import { apiClient } from './client';
import { ApiResponse, FollowUp, PageResponse } from '../types';

export const followUpApi = {
  getFollowUps: async (params?: {
    status?: string;
    period?: 'overdue' | 'today' | 'upcoming';
    page?: number;
    size?: number;
  }): Promise<PageResponse<FollowUp>> => {
    if (params?.period && ['today', 'upcoming', 'overdue'].includes(params.period)) {
      const res = await apiClient.get<ApiResponse<FollowUp[]>>(`/follow-ups/${params.period}`);
      const list = res.data.data || [];
      return {
        content: list,
        totalElements: list.length,
        totalPages: 1,
        last: true,
      };
    }
    const res = await apiClient.get<ApiResponse<PageResponse<FollowUp>>>('/follow-ups', { params });
    return res.data.data;
  },

  getTodayFollowUps: async (): Promise<FollowUp[]> => {
    const res = await apiClient.get<ApiResponse<FollowUp[]>>('/follow-ups/today');
    return res.data.data || [];
  },

  getUpcomingFollowUps: async (): Promise<FollowUp[]> => {
    const res = await apiClient.get<ApiResponse<FollowUp[]>>('/follow-ups/upcoming');
    return res.data.data || [];
  },

  getOverdueFollowUps: async (): Promise<FollowUp[]> => {
    const res = await apiClient.get<ApiResponse<FollowUp[]>>('/follow-ups/overdue');
    return res.data.data || [];
  },

  createFollowUp: async (payload: {
    leadId: number;
    scheduledTime: string;
    notes?: string;
  }): Promise<FollowUp> => {
    // Format to ISO without trailing Z/millis and with seconds for Jackson LocalDateTime compatibility
    let cleanDate = payload.scheduledTime;
    if (cleanDate) {
      cleanDate = cleanDate.replace(/Z$/, '');
      if (cleanDate.includes('.')) {
        cleanDate = cleanDate.split('.')[0];
      }
      if (cleanDate.length === 16) {
        cleanDate = cleanDate + ':00';
      }
    }
    const res = await apiClient.post<ApiResponse<FollowUp>>('/follow-ups', {
      ...payload,
      scheduledTime: cleanDate,
    });
    return res.data.data;
  },

  toggleStatus: async (id: number, status: 'COMPLETED' | 'CANCELLED' | 'PENDING' | string): Promise<FollowUp> => {
    const res = await apiClient.patch<ApiResponse<FollowUp>>(`/follow-ups/${id}/status`, { status });
    return res.data.data;
  },
};
