import { apiClient } from './client';
import { ApiResponse, GoogleSheetsSyncLog, PageResponse } from '../types';

export const sheetsApi = {
  triggerSync: async (): Promise<GoogleSheetsSyncLog> => {
    const res = await apiClient.post<ApiResponse<GoogleSheetsSyncLog>>('/google-sheets/sync', {}, { timeout: 120000 });
    return res.data.data;
  },

  getSyncStatus: async (): Promise<GoogleSheetsSyncLog> => {
    const res = await apiClient.get<ApiResponse<GoogleSheetsSyncLog>>('/google-sheets/status');
    return res.data.data;
  },

  getSyncHistory: async (params?: { page?: number; size?: number }): Promise<PageResponse<GoogleSheetsSyncLog>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<GoogleSheetsSyncLog>>>('/google-sheets/history', { params });
    return res.data.data;
  },
};
