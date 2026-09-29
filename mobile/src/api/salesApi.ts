import { apiClient } from './client';
import { ApiResponse, Sale } from '../types';

export const salesApi = {
  getMySales: async (): Promise<Sale[]> => {
    const res = await apiClient.get<ApiResponse<Sale[]>>('/sales/my-sales');
    return res.data.data;
  },

  getAllSales: async (): Promise<Sale[]> => {
    const res = await apiClient.get<ApiResponse<Sale[]>>('/sales/all');
    return res.data.data;
  },

  convertLead: async (payload: { leadId: number; dealValue: number; notes?: string }): Promise<Sale> => {
    const res = await apiClient.post<ApiResponse<Sale>>('/sales/convert', payload);
    return res.data.data;
  },
};
