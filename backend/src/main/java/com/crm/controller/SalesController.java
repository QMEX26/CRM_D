package com.crm.controller;

import com.crm.dto.request.ConversionRequest;
import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.SaleResponse;
import com.crm.security.CurrentUser;
import com.crm.security.UserPrincipal;
import com.crm.service.SalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping({"/leads/{id}/convert", "/sales/convert"})
    public ResponseEntity<ApiResponse<SaleResponse>> convertLead(
            @PathVariable(required = false) Long id,
            @RequestBody ConversionRequest request,
            @CurrentUser UserPrincipal principal) {

        Long targetLeadId = id != null ? id : (request != null ? request.getLeadId() : null);
        SaleResponse response = salesService.convertLead(targetLeadId, request, principal.getId(), principal.isAdmin());
        return ResponseEntity.ok(ApiResponse.ok("Lead converted successfully!", response));
    }

    @GetMapping("/sales/my-sales")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getMySales(@CurrentUser UserPrincipal principal) {
        List<SaleResponse> responses = salesService.getSalesByUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/sales/all")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getAllSales(@CurrentUser UserPrincipal principal) {
        List<SaleResponse> responses = salesService.getAllSales();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
