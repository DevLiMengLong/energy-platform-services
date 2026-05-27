package com.getech.energy.platformbasic.catalog;

import com.getech.energy.platformbasic.auth.AuthContext;
import com.getech.energy.platformbasic.auth.CurrentUser;
import com.getech.energy.platformbasic.common.ApiException;
import com.getech.energy.platformbasic.common.ApiResponse;
import com.getech.energy.platformbasic.common.PageResult;
import com.getech.energy.platformbasic.common.TraceContext;
import com.getech.energy.platformbasic.logging.LogClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform")
public class PlatformController {

    private final ResourceService resourceService;
    private final TreeService treeService;
    private final MutationService mutationService;
    private final LogClient logClient;

    public PlatformController(ResourceService resourceService, TreeService treeService, MutationService mutationService,
                              LogClient logClient) {
        this.resourceService = resourceService;
        this.treeService = treeService;
        this.mutationService = mutationService;
        this.logClient = logClient;
    }

    @GetMapping("/tenants")
    public ApiResponse<PageResult> tenants(@RequestParam(name = "keyword", required = false) String keyword,
                                           @RequestParam(name = "page", defaultValue = "1") int page,
                                           @RequestParam(name = "size", defaultValue = "10") int size,
                                           HttpServletRequest request) {
        requirePlatformAdmin();
        return ApiResponse.ok(resourceService.page("platform.tenants", AuthContext.requireUser(), keyword, page, size, filters(request)),
                TraceContext.getTraceId(request));
    }

    @PostMapping("/tenants")
    public ApiResponse<Map<String, Object>> createTenant(@Valid @RequestBody TenantRequest body, HttpServletRequest request) {
        CurrentUser user = requirePlatformAdmin();
        Map<String, Object> result = mutationService.createTenant(body);
        logClient.operation(TraceContext.getTraceId(request), user, "platform.tenants", "CREATE", "新增租户",
                "basic_tenant", String.valueOf(result.get("id")), request, true, "tenant created");
        return ApiResponse.ok(result, TraceContext.getTraceId(request));
    }

    @PutMapping("/tenants/{id}")
    public ApiResponse<Void> updateTenantPlaceholder(@PathVariable("id") Long id, HttpServletRequest request) {
        CurrentUser user = requirePlatformAdmin();
        logClient.operation(TraceContext.getTraceId(request), user, "platform.tenants", "UPDATE", "编辑租户",
                "basic_tenant", String.valueOf(id), request, true, "tenant update accepted");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @PostMapping("/tenants/{id}/disable")
    public ApiResponse<Void> disableTenant(@PathVariable("id") Long id, HttpServletRequest request) {
        CurrentUser user = requirePlatformAdmin();
        logClient.operation(TraceContext.getTraceId(request), user, "platform.tenants", "DISABLE", "停用租户",
                "basic_tenant", String.valueOf(id), request, true, "tenant disable accepted");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @GetMapping("/tenant-admins")
    public ApiResponse<PageResult> tenantAdmins(@RequestParam(name = "keyword", required = false) String keyword,
                                                @RequestParam(name = "page", defaultValue = "1") int page,
                                                @RequestParam(name = "size", defaultValue = "10") int size,
                                                HttpServletRequest request) {
        requirePlatformAdmin();
        return ApiResponse.ok(resourceService.page("platform.tenant-admins", AuthContext.requireUser(), keyword, page, size, filters(request)),
                TraceContext.getTraceId(request));
    }

    @PostMapping("/tenant-admins/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable("id") Long id, HttpServletRequest request) {
        CurrentUser user = requirePlatformAdmin();
        logClient.operation(TraceContext.getTraceId(request), user, "platform.tenant-admins", "RESET_PASSWORD", "重置密码",
                "basic_user", String.valueOf(id), request, true, "password reset accepted");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @GetMapping("/subsystems")
    public ApiResponse<PageResult> subsystems(@RequestParam(name = "keyword", required = false) String keyword,
                                              @RequestParam(name = "page", defaultValue = "1") int page,
                                              @RequestParam(name = "size", defaultValue = "10") int size,
                                              HttpServletRequest request) {
        requirePlatformAdmin();
        return ApiResponse.ok(resourceService.page("platform.subsystems", AuthContext.requireUser(), keyword, page, size, filters(request)),
                TraceContext.getTraceId(request));
    }

    @GetMapping("/menus/tree")
    public ApiResponse<List<Map<String, Object>>> menus(@RequestParam(name = "subsystemCode", required = false) String subsystemCode,
                                                        @RequestParam(name = "keyword", required = false) String keyword,
                                                        HttpServletRequest request) {
        requirePlatformAdmin();
        return ApiResponse.ok(treeService.platformMenuTree(subsystemCode, keyword), TraceContext.getTraceId(request));
    }

    @GetMapping("/tenant-permissions")
    public ApiResponse<PageResult> tenantPermissions(@RequestParam(name = "keyword", required = false) String keyword,
                                                     @RequestParam(name = "page", defaultValue = "1") int page,
                                                     @RequestParam(name = "size", defaultValue = "10") int size,
                                                     HttpServletRequest request) {
        requirePlatformAdmin();
        return ApiResponse.ok(resourceService.page("platform.tenant-permissions", AuthContext.requireUser(), keyword, page, size, filters(request)),
                TraceContext.getTraceId(request));
    }

    @PostMapping("/tenant-permissions")
    public ApiResponse<Void> saveTenantPermissions(@Valid @RequestBody TenantPermissionRequest body,
                                                   HttpServletRequest request) {
        CurrentUser user = requirePlatformAdmin();
        mutationService.saveTenantPermissions(body);
        logClient.operation(TraceContext.getTraceId(request), user, "platform.tenant-permissions", "SAVE", "分配租户权限",
                "basic_tenant_permission", String.valueOf(body.tenantId()), request, true, "tenant permissions saved");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @PostMapping("/actions")
    public ApiResponse<Map<String, Object>> acceptAction(@Valid @RequestBody CatalogActionRequest body,
                                                         HttpServletRequest request) {
        CurrentUser user = requirePlatformAdmin();
        logClient.operation(TraceContext.getTraceId(request), user, body.moduleCode(), body.actionCode(), body.actionName(),
                body.moduleCode(), body.targetId() == null ? null : String.valueOf(body.targetId()), request, true,
                "platform action accepted");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", true);
        result.put("moduleCode", body.moduleCode());
        result.put("actionCode", body.actionCode());
        result.put("targetId", body.targetId());
        return ApiResponse.ok(result, TraceContext.getTraceId(request));
    }

    private CurrentUser requirePlatformAdmin() {
        CurrentUser user = AuthContext.requireUser();
        if (!user.platformAdmin()) {
            throw new ApiException("FORBIDDEN", "Platform administrator permission is required");
        }
        return user;
    }

    private Map<String, String> filters(HttpServletRequest request) {
        Map<String, String> filters = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if ("page".equals(key) || "size".equals(key) || "keyword".equals(key) || values.length == 0) {
                return;
            }
            filters.put(key, values[0]);
        });
        return filters;
    }
}
