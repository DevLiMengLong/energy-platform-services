package com.getech.energy.platformbasic.catalog;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserGroupMembersRequest(
        @NotNull(message = "user ids are required") List<Long> userIds
) {
}
