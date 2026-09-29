package com.crm.service.impl;

import com.crm.dto.request.ConversionRequest;
import com.crm.dto.response.SaleResponse;
import com.crm.exception.ResourceNotFoundException;
import com.crm.mapper.SaleMapper;
import com.crm.model.Lead;
import com.crm.model.Sale;
import com.crm.model.User;
import com.crm.repository.LeadRepository;
import com.crm.repository.SalesRepository;
import com.crm.repository.UserRepository;
import com.crm.service.AuditService;
import com.crm.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.crm.exception.BusinessException;
import com.crm.exception.ForbiddenException;
import com.crm.service.LeadAssignmentService;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements SalesService {

    private final SalesRepository salesRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final LeadAssignmentService leadAssignmentService;
    private final SaleMapper saleMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public SaleResponse convertLead(Long leadId, ConversionRequest request, Long userId, boolean isAdmin) {
        if (leadId == null) {
            throw new BusinessException("Lead ID is required for conversion");
        }

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

        if (!leadAssignmentService.isUserAllowedToAccessLead(leadId, userId, isAdmin)) {
            throw new ForbiddenException("Access denied: You are not authorized to convert this lead");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update lead status and outcome
        lead.setStatus("CONVERTED");
        lead.setBusinessOutcome("CONVERTED");
        leadRepository.save(lead);

        Sale sale = salesRepository.findByLeadId(leadId).orElse(null);
        if (sale == null) {
            sale = Sale.builder()
                    .lead(lead)
                    .user(user)
                    .dealValue(request.getDealValue())
                    .notes(request.getNotes())
                    .convertedAt(LocalDateTime.now())
                    .build();
        } else {
            sale.setDealValue(request.getDealValue());
            sale.setNotes(request.getNotes());
            sale.setUser(user);
        }

        Sale saved = salesRepository.save(sale);

        auditService.logAction(userId, "Lead", lead.getId(), "CONVERT", null,
                "Lead converted! Value: " + saved.getDealValue());

        return saleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sale> getSaleByLead(Long leadId) {
        return salesRepository.findByLeadId(leadId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesByUser(Long userId) {
        return salesRepository.findByUserIdOrderByConvertedAtDesc(userId).stream()
                .map(saleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getAllSales() {
        return salesRepository.findAllByOrderByConvertedAtDesc().stream()
                .map(saleMapper::toResponse)
                .toList();
    }
}
