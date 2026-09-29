package com.crm.dto.request;

import java.math.BigDecimal;

public class ConversionRequest {
    private Long leadId;
    private BigDecimal dealValue;
    private String notes;

    public ConversionRequest() {}

    public ConversionRequest(Long leadId, BigDecimal dealValue, String notes) {
        this.leadId = leadId;
        this.dealValue = dealValue;
        this.notes = notes;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public BigDecimal getDealValue() {
        return dealValue;
    }

    public void setDealValue(BigDecimal dealValue) {
        this.dealValue = dealValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
