import { apiClient } from './client';
import { ApiResponse, Attendance, AttendanceMonthlyResponse } from '../types';

export const attendanceApi = {
  getTodayAttendance: async (): Promise<Attendance> => {
    const res = await apiClient.get<ApiResponse<Attendance>>('/attendance/today');
    return res.data.data;
  },

  clockIn: async (): Promise<Attendance> => {
    const res = await apiClient.post<ApiResponse<Attendance>>('/attendance/clock-in');
    return res.data.data;
  },

  clockOut: async (): Promise<Attendance> => {
    const res = await apiClient.post<ApiResponse<Attendance>>('/attendance/clock-out');
    return res.data.data;
  },

  getMonthlyAttendance: async (year: number, month: number, userId?: number): Promise<AttendanceMonthlyResponse> => {
    const res = await apiClient.get<ApiResponse<AttendanceMonthlyResponse>>('/attendance/monthly', {
      params: { year, month, userId },
    });
    return res.data.data;
  },

  getUserMonthlyAttendance: async (userId: number, year: number, month: number): Promise<AttendanceMonthlyResponse> => {
    const res = await apiClient.get<ApiResponse<AttendanceMonthlyResponse>>(`/attendance/users/${userId}/monthly`, {
      params: { year, month },
    });
    return res.data.data;
  },
};
