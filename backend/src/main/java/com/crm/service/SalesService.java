package com.crm.service;

import com.crm.dto.request.ConversionRequest;
import com.crm.dto.response.SaleResponse;
import com.crm.model.Sale;

import java.util.List;
import java.util.Optional;

public interface SalesService {
    SaleResponse convertLead(Long leadId, ConversionRequest request, Long userId, boolean isAdmin);
    Optional<Sale> getSaleByLead(Long leadId);
    List<SaleResponse> getSalesByUser(Long userId);
    List<SaleResponse> getAllSales();
}
