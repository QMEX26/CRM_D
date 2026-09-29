package com.crm.service.impl;

import com.crm.dto.response.*;
import com.crm.model.*;
import com.crm.repository.*;
import com.crm.service.CallService;
import com.crm.service.LeadService;
import com.crm.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final LeadService leadService;
    private final CallService callService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final LeadAssignmentRepository leadAssignmentRepository;
    private final CallRepository callRepository;
    private final FollowUpRepository followUpRepository;
    private final SalesRepository salesRepository;
    private final LeadRepository leadRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LeadSummaryResponse> getLeadReport(Long projectId, String status, String outcome, Pageable pageable) {
        return leadService.searchLeads(projectId, status, outcome, null, null, true, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CallResponse> getCallReport(Long userId, Long projectId, String status, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return callService.searchCalls(userId, null, projectId, status, null, start, end, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEmployeeActivityReport() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", user.getId());
            map.put("userName", user.getName());
            map.put("role", user.getRole().getName());
            map.put("status", user.getStatus());

            long assignedLeads = leadAssignmentRepository.countByUserIdAndIsActiveTrue(user.getId());
            long totalCalls = callRepository.countByUserIdAndCreatedAtBetween(user.getId(), LocalDateTime.MIN, LocalDateTime.MAX);
            long totalDuration = callRepository.sumDurationByUserId(user.getId());
            long conversions = salesRepository.countByUserId(user.getId());
            BigDecimal totalRevenue = salesRepository.sumDealValueByUserId(user.getId());

            map.put("assignedLeads", assignedLeads);
            map.put("totalCalls", totalCalls);
            map.put("totalDurationSeconds", totalDuration);
            map.put("conversions", conversions);
            map.put("totalRevenue", totalRevenue);

            report.add(map);
        }

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProjectReport() {
        List<Project> projects = projectRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (Project project : projects) {
            Map<String, Object> map = new HashMap<>();
            map.put("projectId", project.getId());
            map.put("projectName", project.getName());
            map.put("status", project.getStatus());

            long totalLeads = leadRepository.countByProjectId(project.getId());
            long conversions = salesRepository.countByProjectId(project.getId());

            map.put("totalLeads", totalLeads);
            map.put("conversions", conversions);
            map.put("conversionRate", totalLeads > 0 ? ((double) conversions / totalLeads) * 100.0 : 0.0);

            report.add(map);
        }

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSalesReport(LocalDateTime start, LocalDateTime end) {
        LocalDateTime s = start != null ? start : LocalDateTime.now().minusMonths(1);
        LocalDateTime e = end != null ? end : LocalDateTime.now();

        List<Map<String, Object>> report = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("totalConversions", salesRepository.countByConvertedAtBetween(s, e));
        map.put("totalRevenue", salesRepository.sumTotalDealValue());
        map.put("startDate", s);
        map.put("endDate", e);
        report.add(map);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAnalyticsDashboardResponse getAdminAnalyticsDashboard(Long projectId, Long userId, String leadStatus, String callStatus, String callDirection, LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Filter Calls
        List<Call> allCalls = callRepository.findAll();
        List<Call> filteredCalls = new ArrayList<>();
        for (Call c : allCalls) {
            if (start != null && c.getCreatedAt() != null && c.getCreatedAt().isBefore(start)) continue;
            if (end != null && c.getCreatedAt() != null && c.getCreatedAt().isAfter(end)) continue;
            if (userId != null && (c.getUser() == null || !userId.equals(c.getUser().getId()))) continue;
            if (projectId != null && (c.getLead() == null || c.getLead().getProject() == null || !projectId.equals(c.getLead().getProject().getId()))) continue;
            if (callStatus != null && !callStatus.equalsIgnoreCase(c.getCallStatus()) && !callStatus.equalsIgnoreCase(c.getFinalClassification())) continue;
            if (callDirection != null && !callDirection.equalsIgnoreCase(c.getCallDirection())) continue;
            filteredCalls.add(c);
        }

        // 2. Filter Leads
        List<Lead> allLeads = leadRepository.findAll();
        List<Lead> filteredLeads = new ArrayList<>();
        for (Lead l : allLeads) {
            if (start != null && l.getCreatedAt() != null && l.getCreatedAt().isBefore(start)) continue;
            if (end != null && l.getCreatedAt() != null && l.getCreatedAt().isAfter(end)) continue;
            if (projectId != null && (l.getProject() == null || !projectId.equals(l.getProject().getId()))) continue;
            if (leadStatus != null && !leadStatus.equalsIgnoreCase(l.getStatus())) continue;
            if (userId != null) {
                boolean assigned = leadAssignmentRepository.existsByLeadIdAndUserIdAndIsActiveTrue(l.getId(), userId);
                if (!assigned) continue;
            }
            filteredLeads.add(l);
        }

        // 3. Filter Sales
        List<Sale> allSales = salesRepository.findAll();
        List<Sale> filteredSales = new ArrayList<>();
        for (Sale s : allSales) {
            LocalDateTime sDate = s.getConvertedAt() != null ? s.getConvertedAt() : s.getCreatedAt();
            if (start != null && sDate != null && sDate.isBefore(start)) continue;
            if (end != null && sDate != null && sDate.isAfter(end)) continue;
            if (userId != null && (s.getUser() == null || !userId.equals(s.getUser().getId()))) continue;
            if (projectId != null && (s.getLead() == null || s.getLead().getProject() == null || !projectId.equals(s.getLead().getProject().getId()))) continue;
            filteredSales.add(s);
        }

        // 4. Filter FollowUps
        List<FollowUp> allFollowUps = followUpRepository.findAll();
        List<FollowUp> filteredFollowUps = new ArrayList<>();
        for (FollowUp fu : allFollowUps) {
            if (start != null && fu.getScheduledTime() != null && fu.getScheduledTime().isBefore(start)) continue;
            if (end != null && fu.getScheduledTime() != null && fu.getScheduledTime().isAfter(end)) continue;
            if (userId != null && (fu.getUser() == null || !userId.equals(fu.getUser().getId()))) continue;
            if (projectId != null && (fu.getLead() == null || fu.getLead().getProject() == null || !projectId.equals(fu.getLead().getProject().getId()))) continue;
            filteredFollowUps.add(fu);
        }

        // 5. KPIs
        long totalLeads = filteredLeads.size();
        long totalCalls = filteredCalls.size();
        long connectedCalls = 0;
        long totalTalkTimeSeconds = 0;
        long inboundCount = 0;
        long outboundCount = 0;
        Map<String, Long> classificationMap = new HashMap<>();
        Map<String, long[]> timelineAgg = new java.util.TreeMap<>();

        for (Call c : filteredCalls) {
            int duration = c.getDurationSeconds() != null ? c.getDurationSeconds() : 0;
            totalTalkTimeSeconds += duration;
            boolean isMissed = "NOT_ATTENDED".equalsIgnoreCase(c.getCallStatus()) ||
                    "MISSED".equalsIgnoreCase(c.getCallStatus()) ||
                    "FAILED".equalsIgnoreCase(c.getCallStatus()) ||
                    "NO_ANSWER".equalsIgnoreCase(c.getCallStatus());

            if (!isMissed && (duration > 0 || Boolean.TRUE.equals(c.getIsConnected()) || "CONNECTED".equalsIgnoreCase(c.getCallStatus()))) {
                connectedCalls++;
            }

            if ("INBOUND".equalsIgnoreCase(c.getCallDirection())) {
                inboundCount++;
            } else {
                outboundCount++;
            }

            String classification = c.getFinalClassification() != null ? c.getFinalClassification() : (c.getCallStatus() != null ? c.getCallStatus() : "UNKNOWN");
            classificationMap.put(classification, classificationMap.getOrDefault(classification, 0L) + 1);

            // Group by date for timeline chart (e.g. "MMM dd")
            if (c.getCreatedAt() != null) {
                String dayKey = c.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"));
                long[] agg = timelineAgg.computeIfAbsent(dayKey, k -> new long[]{0, 0});
                agg[0]++; // total calls
                if (!isMissed) {
                    agg[1]++; // connected calls
                }
            }
        }

        long missedCalls = Math.max(0, totalCalls - connectedCalls);
        long avgTalkTimeSeconds = connectedCalls > 0 ? totalTalkTimeSeconds / connectedCalls : 0;
        long conversions = filteredSales.size();
        double conversionRate = totalLeads > 0 ? ((double) conversions / totalLeads) * 100.0 : (totalCalls > 0 ? ((double) conversions / totalCalls) * 100.0 : 0.0);

        long pendingFollowUps = 0;
        long completedFollowUps = 0;
        long overdueFollowUps = 0;
        for (FollowUp fu : filteredFollowUps) {
            if ("COMPLETED".equalsIgnoreCase(fu.getStatus())) {
                completedFollowUps++;
            } else if ("PENDING".equalsIgnoreCase(fu.getStatus())) {
                if (fu.getScheduledTime() != null && fu.getScheduledTime().isBefore(now)) {
                    overdueFollowUps++;
                } else {
                    pendingFollowUps++;
                }
            }
        }

        // Lead stage breakdown
        Map<String, Long> stageBreakdown = new HashMap<>();
        Map<String, Long> outcomeBreakdown = new HashMap<>();
        for (Lead l : filteredLeads) {
            String st = l.getStatus() != null ? l.getStatus() : "NEW";
            stageBreakdown.put(st, stageBreakdown.getOrDefault(st, 0L) + 1);

            String out = l.getBusinessOutcome() != null ? l.getBusinessOutcome() : "NONE";
            outcomeBreakdown.put(out, outcomeBreakdown.getOrDefault(out, 0L) + 1);
        }

        // Timeline list
        List<AdminAnalyticsDashboardResponse.TimePoint> timelineList = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : timelineAgg.entrySet()) {
            timelineList.add(new AdminAnalyticsDashboardResponse.TimePoint(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }

        // Project Performance
        List<Project> targetProjects = (projectId != null)
                ? projectRepository.findById(projectId).map(List::of).orElse(List.of())
                : projectRepository.findAll();
        List<AdminAnalyticsDashboardResponse.ProjectStat> projectStats = new ArrayList<>();
        for (Project p : targetProjects) {
            long pLeads = leadRepository.countByProjectId(p.getId());
            long pConversions = salesRepository.countByProjectId(p.getId());
            long pCalls = filteredCalls.stream().filter(c -> c.getLead() != null && c.getLead().getProject() != null && p.getId().equals(c.getLead().getProject().getId())).count();
            double pRate = pLeads > 0 ? ((double) pConversions / pLeads) * 100.0 : 0.0;
            projectStats.add(new AdminAnalyticsDashboardResponse.ProjectStat(p.getId(), p.getName(), pLeads, pCalls, pConversions, pRate));
        }

        // User Activity (Factual, no leaderboard)
        List<User> targetUsers = (userId != null)
                ? userRepository.findById(userId).map(List::of).orElse(List.of())
                : userRepository.findAll();
        List<AdminAnalyticsDashboardResponse.UserStat> userStats = new ArrayList<>();
        for (User u : targetUsers) {
            long uAssigned = leadAssignmentRepository.countByUserIdAndIsActiveTrue(u.getId());
            long uCalls = 0;
            long uConnected = 0;
            long uTalk = 0;
            for (Call c : filteredCalls) {
                if (c.getUser() != null && u.getId().equals(c.getUser().getId())) {
                    uCalls++;
                    int dur = c.getDurationSeconds() != null ? c.getDurationSeconds() : 0;
                    uTalk += dur;
                    if (dur > 0 || Boolean.TRUE.equals(c.getIsConnected())) {
                        uConnected++;
                    }
                }
            }
            long uConversions = salesRepository.countByUserId(u.getId());
            long uFollowUps = filteredFollowUps.stream().filter(fu -> fu.getUser() != null && u.getId().equals(fu.getUser().getId())).count();
            userStats.add(new AdminAnalyticsDashboardResponse.UserStat(
                    u.getId(),
                    u.getName(),
                    u.getRole() != null ? u.getRole().getName() : "AGENT",
                    uAssigned,
                    uCalls,
                    uConnected,
                    uTalk,
                    uConversions,
                    uFollowUps
            ));
        }

        return AdminAnalyticsDashboardResponse.builder()
                .kpis(AdminAnalyticsDashboardResponse.KpiMetrics.builder()
                        .totalLeads(totalLeads)
                        .totalCalls(totalCalls)
                        .connectedCalls(connectedCalls)
                        .missedCalls(missedCalls)
                        .totalTalkTimeSeconds(totalTalkTimeSeconds)
                        .avgTalkTimeSeconds(avgTalkTimeSeconds)
                        .conversions(conversions)
                        .conversionRate(conversionRate)
                        .pendingFollowUps(pendingFollowUps)
                        .overdueFollowUps(overdueFollowUps)
                        .build())
                .callPerformance(AdminAnalyticsDashboardResponse.CallPerformanceMetrics.builder()
                        .connectedCount(connectedCalls)
                        .missedCount(missedCalls)
                        .inboundCount(inboundCount)
                        .outboundCount(outboundCount)
                        .classificationBreakdown(classificationMap)
                        .timelineData(timelineList)
                        .build())
                .leadPerformance(AdminAnalyticsDashboardResponse.LeadPerformanceMetrics.builder()
                        .stageBreakdown(stageBreakdown)
                        .outcomeBreakdown(outcomeBreakdown)
                        .conversionRate(conversionRate)
                        .build())
                .projectPerformance(projectStats)
                .userActivity(userStats)
                .followUpReport(AdminAnalyticsDashboardResponse.FollowUpMetrics.builder()
                        .pendingCount(pendingFollowUps)
                        .completedCount(completedFollowUps)
                        .overdueCount(overdueFollowUps)
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportReportCsv(String reportType, Long projectId, Long userId, String leadStatus, String callStatus, String callDirection, LocalDateTime start, LocalDateTime end) {
        StringBuilder csv = new StringBuilder();
        String type = reportType != null ? reportType.toUpperCase() : "LEADS";

        if ("CALLS".equals(type)) {
            csv.append("Call ID,Lead Name,Customer Phone,User,Direction,Status,Classification,Duration (Seconds),Created At,Notes\n");
            List<Call> calls = callRepository.findAll();
            for (Call c : calls) {
                if (start != null && c.getCreatedAt() != null && c.getCreatedAt().isBefore(start)) continue;
                if (end != null && c.getCreatedAt() != null && c.getCreatedAt().isAfter(end)) continue;
                if (userId != null && (c.getUser() == null || !userId.equals(c.getUser().getId()))) continue;
                if (projectId != null && (c.getLead() == null || c.getLead().getProject() == null || !projectId.equals(c.getLead().getProject().getId()))) continue;
                if (callStatus != null && !callStatus.equalsIgnoreCase(c.getCallStatus()) && !callStatus.equalsIgnoreCase(c.getFinalClassification())) continue;
                if (callDirection != null && !callDirection.equalsIgnoreCase(c.getCallDirection())) continue;

                csv.append(c.getId()).append(",")
                        .append(csvEscape(c.getLead() != null ? c.getLead().getName() : "Unknown")).append(",")
                        .append(csvEscape(c.getPhoneNumber())).append(",")
                        .append(csvEscape(c.getUser() != null ? c.getUser().getName() : "Unknown")).append(",")
                        .append(csvEscape(c.getCallDirection())).append(",")
                        .append(csvEscape(c.getCallStatus())).append(",")
                        .append(csvEscape(c.getFinalClassification())).append(",")
                        .append(c.getDurationSeconds() != null ? c.getDurationSeconds() : 0).append(",")
                        .append(c.getCreatedAt() != null ? c.getCreatedAt().toString() : "").append(",")
                        .append(csvEscape(c.getNotes())).append("\n");
            }
        } else if ("CONVERSIONS".equals(type) || "SALES".equals(type)) {
            csv.append("Sale ID,Lead Name,Customer Phone,Project,Closed By,Deal Value,Converted At,Notes\n");
            List<Sale> sales = salesRepository.findAll();
            for (Sale s : sales) {
                LocalDateTime sDate = s.getConvertedAt() != null ? s.getConvertedAt() : s.getCreatedAt();
                if (start != null && sDate != null && sDate.isBefore(start)) continue;
                if (end != null && sDate != null && sDate.isAfter(end)) continue;
                if (userId != null && (s.getUser() == null || !userId.equals(s.getUser().getId()))) continue;
                if (projectId != null && (s.getLead() == null || s.getLead().getProject() == null || !projectId.equals(s.getLead().getProject().getId()))) continue;

                csv.append(s.getId()).append(",")
                        .append(csvEscape(s.getLead() != null ? s.getLead().getName() : "Unknown")).append(",")
                        .append(csvEscape(s.getLead() != null ? s.getLead().getPhone() : "")).append(",")
                        .append(csvEscape(s.getLead() != null && s.getLead().getProject() != null ? s.getLead().getProject().getName() : "")).append(",")
                        .append(csvEscape(s.getUser() != null ? s.getUser().getName() : "Unknown")).append(",")
                        .append(s.getDealValue() != null ? s.getDealValue().toString() : "0.00").append(",")
                        .append(sDate != null ? sDate.toString() : "").append(",")
                        .append(csvEscape(s.getNotes())).append("\n");
            }
        } else if ("FOLLOWUPS".equals(type)) {
            csv.append("FollowUp ID,Lead Name,Customer Phone,Assigned User,Scheduled Time,Status,Notes\n");
            List<FollowUp> followUps = followUpRepository.findAll();
            for (FollowUp fu : followUps) {
                if (start != null && fu.getScheduledTime() != null && fu.getScheduledTime().isBefore(start)) continue;
                if (end != null && fu.getScheduledTime() != null && fu.getScheduledTime().isAfter(end)) continue;
                if (userId != null && (fu.getUser() == null || !userId.equals(fu.getUser().getId()))) continue;
                if (projectId != null && (fu.getLead() == null || fu.getLead().getProject() == null || !projectId.equals(fu.getLead().getProject().getId()))) continue;

                csv.append(fu.getId()).append(",")
                        .append(csvEscape(fu.getLead() != null ? fu.getLead().getName() : "Unknown")).append(",")
                        .append(csvEscape(fu.getLead() != null ? fu.getLead().getPhone() : "")).append(",")
                        .append(csvEscape(fu.getUser() != null ? fu.getUser().getName() : "Unknown")).append(",")
                        .append(fu.getScheduledTime() != null ? fu.getScheduledTime().toString() : "").append(",")
                        .append(csvEscape(fu.getStatus())).append(",")
                        .append(csvEscape(fu.getNotes())).append("\n");
            }
        } else {
            // Default LEADS
            csv.append("Lead ID,Lead Name,Phone,Email,City,Project,Status,Outcome,Created At\n");
            List<Lead> leads = leadRepository.findAll();
            for (Lead l : leads) {
                if (start != null && l.getCreatedAt() != null && l.getCreatedAt().isBefore(start)) continue;
                if (end != null && l.getCreatedAt() != null && l.getCreatedAt().isAfter(end)) continue;
                if (projectId != null && (l.getProject() == null || !projectId.equals(l.getProject().getId()))) continue;
                if (leadStatus != null && !leadStatus.equalsIgnoreCase(l.getStatus())) continue;
                if (userId != null) {
                    boolean assigned = leadAssignmentRepository.existsByLeadIdAndUserIdAndIsActiveTrue(l.getId(), userId);
                    if (!assigned) continue;
                }

                csv.append(l.getId()).append(",")
                        .append(csvEscape(l.getName())).append(",")
                        .append(csvEscape(l.getPhone())).append(",")
                        .append(csvEscape(l.getEmail())).append(",")
                        .append(csvEscape(l.getCity())).append(",")
                        .append(csvEscape(l.getProject() != null ? l.getProject().getName() : "")).append(",")
                        .append(csvEscape(l.getStatus())).append(",")
                        .append(csvEscape(l.getBusinessOutcome())).append(",")
                        .append(l.getCreatedAt() != null ? l.getCreatedAt().toString() : "").append("\n");
            }
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String csvEscape(String val) {
        if (val == null) return "\"\"";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }
}
