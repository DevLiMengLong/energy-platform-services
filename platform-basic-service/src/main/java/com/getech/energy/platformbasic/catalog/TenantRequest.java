package com.getech.energy.platformbasic.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantRequest(
        @Size(max = 64, message = "tenant mark length must be <= 64") String tenantMark,
        @NotBlank(message = "tenant name is required") @Size(max = 128, message = "tenant name length must be <= 128") String tenantName,
        @Size(max = 64, message = "industry length must be <= 64") String industry,
        @Size(max = 64, message = "contact name length must be <= 64") String contactName,
        @Size(max = 32, message = "contact phone length must be <= 32") String contactPhone,
        String status
) {
}
