package com.crm.mapper;

import com.crm.dto.response.SaleResponse;
import com.crm.model.Lead;
import com.crm.model.Sale;
import org.springframework.stereotype.Component;

@Component
public class SaleMapper {

    public SaleResponse toResponse(Sale sale) {
        if (sale == null) return null;

        Lead lead = sale.getLead();
        return SaleResponse.builder()
                .id(sale.getId())
                .leadId(lead != null ? lead.getId() : null)
                .leadName(lead != null ? lead.getName() : null)
                .leadPhone(lead != null ? lead.getPhone() : null)
                .projectId(lead != null && lead.getProject() != null ? lead.getProject().getId() : null)
                .projectName(lead != null && lead.getProject() != null ? lead.getProject().getName() : null)
                .userId(sale.getUser() != null ? sale.getUser().getId() : null)
                .userName(sale.getUser() != null ? sale.getUser().getName() : null)
                .assignedAgentName(sale.getUser() != null ? sale.getUser().getName() : null)
                .status(lead != null ? lead.getStatus() : "CONVERTED")
                .dealValue(sale.getDealValue())
                .notes(sale.getNotes())
                .convertedAt(sale.getConvertedAt())
                .createdAt(sale.getCreatedAt())
                .build();
    }
}
