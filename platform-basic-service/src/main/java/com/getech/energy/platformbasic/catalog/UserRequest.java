package com.getech.energy.platformbasic.catalog;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UserRequest(
        @NotBlank(message = "account is required")
        @Size(max = 64, message = "account length must be <= 64")
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{3,63}$", message = "account format must be 4-64 letters, numbers, underscores and start with a letter")
        String account,
        @NotBlank(message = "username is required")
        @Size(max = 64, message = "username length must be <= 64")
        String username,
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "phone format must be a valid mainland China mobile number")
        String phone,
        @Email(message = "email format must be valid")
        @Size(max = 128, message = "email length must be <= 128")
        String email,
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        @NotEmpty(message = "role is required")
        List<@NotBlank(message = "role is required") @Size(max = 128, message = "role length must be <= 128") String> roleName,
        @NotBlank(message = "organization is required")
        @Size(max = 128, message = "organization length must be <= 128")
        String orgName,
        @Pattern(regexp = "^(|ENABLED|DISABLED)$", message = "status must be ENABLED or DISABLED")
        String status
) {
}
