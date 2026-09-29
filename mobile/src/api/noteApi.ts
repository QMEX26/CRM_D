import { apiClient } from './client';
import { ApiResponse, Note } from '../types';

export const noteApi = {
  getNotes: async (leadId: number): Promise<Note[]> => {
    const res = await apiClient.get<ApiResponse<Note[]>>(`/leads/${leadId}/notes`);
    return res.data.data;
  },

  addNote: async (leadId: number, content: string): Promise<Note> => {
    const res = await apiClient.post<ApiResponse<Note>>(`/leads/${leadId}/notes`, { content });
    return res.data.data;
  },
};
