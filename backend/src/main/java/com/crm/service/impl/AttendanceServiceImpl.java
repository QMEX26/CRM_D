package com.crm.service.impl;

import com.crm.dto.response.AttendanceMonthlyResponse;
import com.crm.dto.response.AttendanceResponse;
import com.crm.exception.BusinessException;
import com.crm.exception.ResourceNotFoundException;
import com.crm.model.Attendance;
import com.crm.model.User;
import com.crm.repository.AttendanceRepository;
import com.crm.repository.UserRepository;
import com.crm.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private static final java.time.ZoneId APP_ZONE = java.time.ZoneId.of("Asia/Kolkata");

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getTodayAttendance(Long userId) {
        LocalDate today = LocalDate.now(APP_ZONE);
        LocalDateTime now = LocalDateTime.now(APP_ZONE);
        User user = userRepository.findById(userId).orElse(null);
        com.crm.model.WorkShift shift = (user != null && user.getShift() != null)
                ? user.getShift()
                : com.crm.model.WorkShift.SHIFT_1000_1900;

        Optional<Attendance> attendanceOpt = attendanceRepository.findByUserIdAndDate(userId, today);
        if (attendanceOpt.isPresent()) {
            AttendanceResponse resp = toResponse(attendanceOpt.get());
            resp.setShift(shift.getId());
            resp.setShiftDisplayName(shift.getDisplayName());
            resp.setShiftStartTime(shift.getStartTime());
            resp.setShiftEndTime(shift.getEndTime());
            resp.setServerTime(now);
            // If already clocked in or clocked out, check-in is NOT overdue
            resp.setCheckInOverdue(false);
            return resp;
        }

        boolean isOverdue = now.toLocalTime().isAfter(shift.getStartTime());

        return AttendanceResponse.builder()
                .userId(userId)
                .userName(user != null ? user.getName() : null)
                .date(today)
                .status("NOT_CLOCKED_IN")
                .clockedIn(false)
                .clockedOut(false)
                .durationMinutes(0)
                .shift(shift.getId())
                .shiftDisplayName(shift.getDisplayName())
                .shiftStartTime(shift.getStartTime())
                .shiftEndTime(shift.getEndTime())
                .checkInOverdue(isOverdue)
                .serverTime(now)
                .build();
    }

    @Override
    @Transactional
    public AttendanceResponse clockIn(Long userId) {
        LocalDate today = LocalDate.now(APP_ZONE);
        Optional<Attendance> existing = attendanceRepository.findByUserIdAndDate(userId, today);

        if (existing.isPresent()) {
            Attendance att = existing.get();
            if (att.getClockOutTime() != null) {
                throw new BusinessException("You have already completed your attendance session for today. Only one session is allowed per calendar day.");
            }
            if (att.getClockInTime() != null) {
                String timeStr = att.getClockInTime().toLocalTime().toString();
                String formatted = timeStr.length() >= 5 ? timeStr.substring(0, 5) : timeStr;
                throw new BusinessException("You have already clocked in today at " + formatted + ".");
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Attendance attendance = existing.orElseGet(() -> Attendance.builder()
                .user(user)
                .date(today)
                .build());

        attendance.setClockInTime(LocalDateTime.now(APP_ZONE));
        attendance.setStatus("PRESENT");
        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse clockOut(Long userId) {
        LocalDate today = LocalDate.now(APP_ZONE);
        Attendance attendance = attendanceRepository.findByUserIdAndDate(userId, today)
                .orElseThrow(() -> new BusinessException("You must clock in before clocking out."));

        if (attendance.getClockInTime() == null) {
            throw new BusinessException("You have not clocked in today.");
        }

        if (attendance.getClockOutTime() != null) {
            throw new BusinessException("You have already clocked out today. Only one attendance session is allowed per calendar day.");
        }

        LocalDateTime now = LocalDateTime.now(APP_ZONE);
        attendance.setClockOutTime(now);

        long minutes = Math.max(0, Duration.between(attendance.getClockInTime(), now).toMinutes());
        attendance.setDurationMinutes((int) minutes);

        // If working duration is less than 4 hours (240 minutes), classify as HALF_DAY
        if (minutes < 240) {
            attendance.setStatus("HALF_DAY");
        } else {
            attendance.setStatus("PRESENT");
        }

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceMonthlyResponse getMonthlyAttendance(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Attendance> records = attendanceRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, end);

        long fullDayCount = 0;
        long halfDayCount = 0;
        long leaveCount = 0;
        long holidayCount = 0;
        long totalMinutes = 0;

        for (Attendance a : records) {
            Integer dur = a.getDurationMinutes();
            int d = (dur != null) ? dur : 0;
            totalMinutes += d;

            if ("HALF_DAY".equalsIgnoreCase(a.getStatus())) {
                halfDayCount++;
            } else if ("PRESENT".equalsIgnoreCase(a.getStatus())) {
                if (d >= 240 || a.getClockOutTime() == null) {
                    fullDayCount++;
                } else {
                    halfDayCount++;
                }
            } else if ("LEAVE".equalsIgnoreCase(a.getStatus())) {
                leaveCount++;
            } else if ("HOLIDAY".equalsIgnoreCase(a.getStatus())) {
                holidayCount++;
            }
        }

        long presentCount = fullDayCount + halfDayCount;
        long totalDaysInMonth = ym.lengthOfMonth();

        LocalDate today = LocalDate.now(APP_ZONE);
        long elapsedDays = 0;
        if (year < today.getYear() || (year == today.getYear() && month < today.getMonthValue())) {
            elapsedDays = totalDaysInMonth;
        } else if (year == today.getYear() && month == today.getMonthValue()) {
            elapsedDays = today.getDayOfMonth();
        } else {
            elapsedDays = 0;
        }

        long offDays = Math.max(0, elapsedDays - (presentCount + leaveCount + holidayCount));

        List<AttendanceResponse> responseList = records.stream().map(this::toResponse).toList();

        return AttendanceMonthlyResponse.builder()
                .year(year)
                .month(month)
                .totalDays(totalDaysInMonth)
                .presentDays(presentCount)
                .fullDays(fullDayCount)
                .halfDays(halfDayCount)
                .offDays(offDays)
                .leaveDays(leaveCount)
                .holidayDays(holidayCount)
                .totalWorkingHours(totalMinutes / 60)
                .records(responseList)
                .build();
    }

    private AttendanceResponse toResponse(Attendance a) {
        User u = a.getUser();
        com.crm.model.WorkShift shift = (u != null && u.getShift() != null)
                ? u.getShift()
                : com.crm.model.WorkShift.SHIFT_1000_1900;

        return AttendanceResponse.builder()
                .id(a.getId())
                .userId(u != null ? u.getId() : null)
                .userName(u != null ? u.getName() : null)
                .date(a.getDate())
                .clockInTime(a.getClockInTime())
                .clockOutTime(a.getClockOutTime())
                .durationMinutes(a.getDurationMinutes() != null ? a.getDurationMinutes() : 0)
                .status(a.getStatus())
                .notes(a.getNotes())
                .clockedIn(a.getClockInTime() != null)
                .clockedOut(a.getClockOutTime() != null)
                .shift(shift.getId())
                .shiftDisplayName(shift.getDisplayName())
                .shiftStartTime(shift.getStartTime())
                .shiftEndTime(shift.getEndTime())
                .checkInOverdue(false)
                .serverTime(LocalDateTime.now(APP_ZONE))
                .build();
    }
}
