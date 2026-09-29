import { apiClient } from './client';
import { ApiResponse, AuditLog, PageResponse } from '../types';

export const auditApi = {
  getAuditLogs: async (params?: {
    entityName?: string;
    action?: string;
    page?: number;
    size?: number;
  }): Promise<PageResponse<AuditLog>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<AuditLog>>>('/audit-logs', { params });
    return res.data.data;
  },
};
