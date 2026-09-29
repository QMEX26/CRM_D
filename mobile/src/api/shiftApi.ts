import { apiClient } from './client';
import { ApiResponse, ShiftOption, ShiftChangeRequest } from '../types';

export const shiftApi = {
  getAvailableShifts: async (): Promise<ShiftOption[]> => {
    const res = await apiClient.get<ApiResponse<ShiftOption[]>>('/shifts');
    return res.data.data;
  },

  createShiftChangeRequest: async (data: {
    requestedShift: string;
    reason?: string;
  }): Promise<ShiftChangeRequest> => {
    const res = await apiClient.post<ApiResponse<ShiftChangeRequest>>('/shifts/requests', data);
    return res.data.data;
  },

  getMyShiftRequests: async (): Promise<ShiftChangeRequest[]> => {
    const res = await apiClient.get<ApiResponse<ShiftChangeRequest[]>>('/shifts/requests/my');
    return res.data.data;
  },

  getPendingShiftRequests: async (): Promise<ShiftChangeRequest[]> => {
    const res = await apiClient.get<ApiResponse<ShiftChangeRequest[]>>('/shifts/requests/pending');
    return res.data.data;
  },

  getAllShiftRequests: async (): Promise<ShiftChangeRequest[]> => {
    const res = await apiClient.get<ApiResponse<ShiftChangeRequest[]>>('/shifts/requests');
    return res.data.data;
  },

  reviewShiftChangeRequest: async (
    id: number,
    data: { status: 'APPROVED' | 'REJECTED'; adminNotes?: string }
  ): Promise<ShiftChangeRequest> => {
    const res = await apiClient.post<ApiResponse<ShiftChangeRequest>>(
      `/shifts/requests/${id}/review`,
      data
    );
    return res.data.data;
  },
};
