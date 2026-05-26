package com.getech.energy.platformbasic.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShiftRequest(
        @Size(max = 64, message = "shift code length must be <= 64") String shiftCode,
        @NotBlank(message = "shift name is required") @Size(max = 128, message = "shift name length must be <= 128") String shiftName,
        @NotBlank(message = "start time is required") @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "start time must be HH:mm") String startTime,
        @NotBlank(message = "end time is required") @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "end time must be HH:mm") String endTime,
        Boolean crossDay,
        String status
) {
}
