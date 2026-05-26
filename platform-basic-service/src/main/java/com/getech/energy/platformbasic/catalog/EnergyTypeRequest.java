package com.getech.energy.platformbasic.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EnergyTypeRequest(
        @Size(max = 64, message = "energy code length must be <= 64") String energyCode,
        @NotBlank(message = "energy name is required") @Size(max = 128, message = "energy name length must be <= 128") String energyName,
        @NotBlank(message = "energy unit is required") @Size(max = 32, message = "energy unit length must be <= 32") String energyUnit,
        @NotNull(message = "standard coal factor is required") @DecimalMin(value = "0.0", message = "standard coal factor must be non-negative") BigDecimal standardCoalFactor,
        @Size(max = 32, message = "standard coal unit length must be <= 32") String standardCoalUnit,
        Integer sortOrder,
        @Size(max = 64, message = "icon length must be <= 64") String icon,
        @Size(max = 512, message = "remark length must be <= 512") String remark,
        String status
) {
}
