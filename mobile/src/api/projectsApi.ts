import { apiClient } from './client';
import { ApiResponse, Project } from '../types';

export const projectsApi = {
  getProjects: async (): Promise<Project[]> => {
    const res = await apiClient.get<ApiResponse<any>>('/projects');
    const data = res.data.data;
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.content)) return data.content;
    return [];
  },

  getProjectById: async (id: number): Promise<Project> => {
    const res = await apiClient.get<ApiResponse<Project>>(`/projects/${id}`);
    return res.data.data;
  },

  createProject: async (data: { name: string; description?: string; status?: string }): Promise<Project> => {
    const res = await apiClient.post<ApiResponse<Project>>('/projects', data);
    return res.data.data;
  },

  updateProject: async (id: number, data: { name: string; description?: string; status?: string }): Promise<Project> => {
    const res = await apiClient.put<ApiResponse<Project>>(`/projects/${id}`, data);
    return res.data.data;
  },

  toggleStatus: async (id: number, status?: string): Promise<Project> => {
    const res = await apiClient.patch<ApiResponse<Project>>(`/projects/${id}/status`, null, {
      params: status ? { status } : undefined,
    });
    return res.data.data;
  },

  deleteProject: async (id: number): Promise<void> => {
    await apiClient.delete(`/projects/${id}`);
  },
};
