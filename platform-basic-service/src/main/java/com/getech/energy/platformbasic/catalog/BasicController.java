package com.getech.energy.platformbasic.catalog;

import com.getech.energy.platformbasic.auth.AuthContext;
import com.getech.energy.platformbasic.auth.CurrentUser;
import com.getech.energy.platformbasic.common.ApiResponse;
import com.getech.energy.platformbasic.common.PageResult;
import com.getech.energy.platformbasic.common.TraceContext;
import com.getech.energy.platformbasic.logging.LogClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/basic")
public class BasicController {

    private final ResourceService resourceService;
    private final TreeService treeService;
    private final MutationService mutationService;
    private final JdbcClient jdbcClient;
    private final LogClient logClient;

    public BasicController(ResourceService resourceService, TreeService treeService, MutationService mutationService,
                           JdbcClient jdbcClient, LogClient logClient) {
        this.resourceService = resourceService;
        this.treeService = treeService;
        this.mutationService = mutationService;
        this.jdbcClient = jdbcClient;
        this.logClient = logClient;
    }

    @GetMapping("/me/menus")
    public ApiResponse<List<Map<String, Object>>> menus(HttpServletRequest request) {
        return ApiResponse.ok(treeService.currentMenus(AuthContext.requireUser()), TraceContext.getTraceId(request));
    }

    @GetMapping("/org-nodes/tree")
    public ApiResponse<List<Map<String, Object>>> orgTree(HttpServletRequest request) {
        return ApiResponse.ok(treeService.orgTree(AuthContext.requireUser()), TraceContext.getTraceId(request));
    }

    @PostMapping("/org-nodes/move")
    public ApiResponse<Void> moveOrg(@Valid @RequestBody MoveOrgRequest body, HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        mutationService.moveOrg(user, body);
        logClient.operation(TraceContext.getTraceId(request), user, "basic.orgNodes", "MOVE", "移动组织",
                "basic_org_node", String.valueOf(body.orgId()), request, true, "organization moved");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @GetMapping("/users")
    public ApiResponse<PageResult> users(@RequestParam(name = "keyword", required = false) String keyword,
                                         @RequestParam(name = "page", defaultValue = "1") int page,
                                         @RequestParam(name = "size", defaultValue = "10") int size,
                                         HttpServletRequest request) {
        return page("basic.users", keyword, page, size, request);
    }

    @GetMapping("/user-groups")
    public ApiResponse<PageResult> userGroups(@RequestParam(name = "keyword", required = false) String keyword,
                                              @RequestParam(name = "page", defaultValue = "1") int page,
                                              @RequestParam(name = "size", defaultValue = "10") int size,
                                              HttpServletRequest request) {
        return page("basic.user-groups", keyword, page, size, request);
    }

    @PostMapping("/user-groups/{id}/members")
    public ApiResponse<Void> replaceUserGroupMembers(@PathVariable("id") Long id,
                                                     @Valid @RequestBody UserGroupMembersRequest body,
                                                     HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        mutationService.replaceUserGroupMembers(user, id, body);
        logClient.operation(TraceContext.getTraceId(request), user, "basic.userGroups", "SAVE_MEMBERS", "维护用户组成员",
                "basic_user_group", String.valueOf(id), request, true, "group members saved");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @GetMapping("/roles")
    public ApiResponse<PageResult> roles(@RequestParam(name = "keyword", required = false) String keyword,
                                         @RequestParam(name = "page", defaultValue = "1") int page,
                                         @RequestParam(name = "size", defaultValue = "10") int size,
                                         HttpServletRequest request) {
        return page("basic.roles", keyword, page, size, request);
    }

    @PostMapping("/roles/{id}/permissions")
    public ApiResponse<Void> saveRolePermissions(@PathVariable("id") Long id, HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        logClient.operation(TraceContext.getTraceId(request), user, "basic.roles", "SAVE_PERMISSIONS", "配置角色权限",
                "basic_role", String.valueOf(id), request, true, "role permissions accepted");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @GetMapping("/dictionaries")
    public ApiResponse<PageResult> dictionaries(@RequestParam(name = "keyword", required = false) String keyword,
                                                @RequestParam(name = "page", defaultValue = "1") int page,
                                                @RequestParam(name = "size", defaultValue = "10") int size,
                                                HttpServletRequest request) {
        return page("basic.dictionaries", keyword, page, size, request);
    }

    @GetMapping("/energy-types")
    public ApiResponse<PageResult> energyTypes(@RequestParam(name = "keyword", required = false) String keyword,
                                               @RequestParam(name = "page", defaultValue = "1") int page,
                                               @RequestParam(name = "size", defaultValue = "10") int size,
                                               HttpServletRequest request) {
        return page("basic.energy-types", keyword, page, size, request);
    }

    @PostMapping("/energy-types")
    public ApiResponse<Map<String, Object>> createEnergyType(@Valid @RequestBody EnergyTypeRequest body,
                                                             HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        Map<String, Object> result = mutationService.createEnergyType(user, body);
        logClient.operation(TraceContext.getTraceId(request), user, "basic.energyTypes", "CREATE", "新增能源类型",
                "basic_energy_type", String.valueOf(result.get("id")), request, true, "energy type created");
        return ApiResponse.ok(result, TraceContext.getTraceId(request));
    }

    @GetMapping("/energy-prices")
    public ApiResponse<PageResult> energyPrices(@RequestParam(name = "keyword", required = false) String keyword,
                                                @RequestParam(name = "page", defaultValue = "1") int page,
                                                @RequestParam(name = "size", defaultValue = "10") int size,
                                                HttpServletRequest request) {
        return page("basic.energy-prices", keyword, page, size, request);
    }

    @GetMapping("/device-models")
    public ApiResponse<PageResult> deviceModels(@RequestParam(name = "keyword", required = false) String keyword,
                                                @RequestParam(name = "page", defaultValue = "1") int page,
                                                @RequestParam(name = "size", defaultValue = "10") int size,
                                                HttpServletRequest request) {
        return page("basic.device-models", keyword, page, size, request);
    }

    @GetMapping("/device-models/{id}/params")
    public ApiResponse<List<Map<String, Object>>> deviceModelParams(@PathVariable("id") Long id, HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        List<Map<String, Object>> rows = jdbcClient.sql("""
                        SELECT id, param_code AS paramCode, param_name AS paramName, data_type AS dataType, unit, status
                        FROM basic_device_model_param
                        WHERE tenant_id = :tenantId AND model_id = :modelId
                        ORDER BY sort_order, id
                        """)
                .param("tenantId", user.tenantId())
                .param("modelId", id)
                .query()
                .listOfRows();
        return ApiResponse.ok(rows, TraceContext.getTraceId(request));
    }

    @GetMapping("/devices")
    public ApiResponse<PageResult> devices(@RequestParam(name = "keyword", required = false) String keyword,
                                           @RequestParam(name = "page", defaultValue = "1") int page,
                                           @RequestParam(name = "size", defaultValue = "10") int size,
                                           HttpServletRequest request) {
        return page("basic.devices", keyword, page, size, request);
    }

    @GetMapping("/devices/{id}/params")
    public ApiResponse<List<Map<String, Object>>> deviceParams(@PathVariable("id") Long id, HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        List<Map<String, Object>> rows = jdbcClient.sql("""
                        SELECT p.id, p.param_name AS paramName, p.param_code AS paramCode, p.unit,
                               cp.collection_model_name AS collectionModelName,
                               cp.collection_model_mark AS collectionModelMark,
                               cp.collection_device_name AS collectionDeviceName,
                               cp.collection_device_mark AS collectionDeviceMark,
                               cp.business_name AS collectionPointName,
                               cp.collection_param_mark AS collectionPointMark
                        FROM basic_device_param p
                        LEFT JOIN basic_device_param_point_binding b ON b.device_param_id = p.id
                        LEFT JOIN basic_collection_point cp ON cp.id = b.collection_point_id
                        WHERE p.tenant_id = :tenantId AND p.device_id = :deviceId
                        ORDER BY p.id
                        """)
                .param("tenantId", user.tenantId())
                .param("deviceId", id)
                .query()
                .listOfRows();
        return ApiResponse.ok(rows, TraceContext.getTraceId(request));
    }

    @PostMapping("/device-params/{id}/bind-point")
    public ApiResponse<Void> bindPoint(@PathVariable("id") Long id, @Valid @RequestBody BindPointRequest body,
                                       HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        mutationService.bindPoint(user, id, body);
        logClient.operation(TraceContext.getTraceId(request), user, "basic.devices", "BIND_POINT", "绑定采集点位",
                "basic_device_param", String.valueOf(id), request, true, "point bound");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @PostMapping("/device-params/{id}/unbind-point")
    public ApiResponse<Void> unbindPoint(@PathVariable("id") Long id, HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        mutationService.unbindPoint(user, id);
        logClient.operation(TraceContext.getTraceId(request), user, "basic.devices", "UNBIND_POINT", "解绑采集点位",
                "basic_device_param", String.valueOf(id), request, true, "point unbound");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @GetMapping("/collection-points")
    public ApiResponse<PageResult> collectionPoints(@RequestParam(name = "keyword", required = false) String keyword,
                                                    @RequestParam(name = "page", defaultValue = "1") int page,
                                                    @RequestParam(name = "size", defaultValue = "10") int size,
                                                    HttpServletRequest request) {
        return page("basic.collection-points", keyword, page, size, request);
    }

    @GetMapping("/stat-models")
    public ApiResponse<PageResult> statModels(@RequestParam(name = "keyword", required = false) String keyword,
                                              @RequestParam(name = "page", defaultValue = "1") int page,
                                              @RequestParam(name = "size", defaultValue = "10") int size,
                                              HttpServletRequest request) {
        return page("basic.stat-models", keyword, page, size, request);
    }

    @GetMapping("/stat-models/{id}/tree")
    public ApiResponse<List<Map<String, Object>>> statModelTree(@PathVariable("id") Long id, HttpServletRequest request) {
        return ApiResponse.ok(treeService.statModelTree(AuthContext.requireUser(), id), TraceContext.getTraceId(request));
    }

    @PostMapping("/stat-models/import-tree")
    public ApiResponse<Void> importStatTree(HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        logClient.operation(TraceContext.getTraceId(request), user, "basic.statModels", "IMPORT_TREE", "导入统计模型树",
                "basic_stat_model_node", null, request, true, "stat tree import accepted");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @PostMapping("/stat-models/import-param-relations")
    public ApiResponse<Void> importStatParamRelations(HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        logClient.operation(TraceContext.getTraceId(request), user, "basic.statModels", "IMPORT_PARAM_RELATIONS", "导入参数关联",
                "basic_stat_node_param_binding", null, request, true, "stat param relation import accepted");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @PostMapping("/stat-nodes/{id}/param-relations")
    public ApiResponse<Void> saveStatParamRelations(@PathVariable("id") Long id, HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        logClient.operation(TraceContext.getTraceId(request), user, "basic.statModels", "SAVE_PARAM_RELATIONS", "保存统计节点参数关联",
                "basic_stat_model_node", String.valueOf(id), request, true, "stat param relations accepted");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @GetMapping("/capacity-centers/tree")
    public ApiResponse<List<Map<String, Object>>> capacityCenters(HttpServletRequest request) {
        return ApiResponse.ok(treeService.capacityTree(AuthContext.requireUser()), TraceContext.getTraceId(request));
    }

    @GetMapping("/capacity-data")
    public ApiResponse<PageResult> capacityData(@RequestParam(name = "keyword", required = false) String keyword,
                                                @RequestParam(name = "page", defaultValue = "1") int page,
                                                @RequestParam(name = "size", defaultValue = "10") int size,
                                                HttpServletRequest request) {
        return page("basic.capacity-data", keyword, page, size, request);
    }

    @PostMapping("/capacity-data/batch-save")
    public ApiResponse<Void> saveCapacityData(@Valid @RequestBody BatchValueRequest body, HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        mutationService.saveCapacityData(user, body);
        logClient.operation(TraceContext.getTraceId(request), user, "basic.capacityCenters", "BATCH_SAVE", "批量保存产能数据",
                "basic_capacity_data", null, request, true, "capacity data saved");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @GetMapping("/unit-consumption-relations")
    public ApiResponse<PageResult> unitRelations(@RequestParam(name = "keyword", required = false) String keyword,
                                                 @RequestParam(name = "page", defaultValue = "1") int page,
                                                 @RequestParam(name = "size", defaultValue = "10") int size,
                                                 HttpServletRequest request) {
        return page("basic.unit-consumption-relations", keyword, page, size, request);
    }

    @PostMapping("/unit-consumption-relations")
    public ApiResponse<Void> saveUnitRelations(HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        logClient.operation(TraceContext.getTraceId(request), user, "basic.unitConsumption", "SAVE", "保存单耗关联",
                "basic_unit_consumption_relation", null, request, true, "unit relation accepted");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @GetMapping("/indicator-data")
    public ApiResponse<PageResult> indicatorData(@RequestParam(name = "keyword", required = false) String keyword,
                                                 @RequestParam(name = "page", defaultValue = "1") int page,
                                                 @RequestParam(name = "size", defaultValue = "10") int size,
                                                 HttpServletRequest request) {
        return page("basic.indicator-data", keyword, page, size, request);
    }

    @PostMapping("/indicator-data/batch-save")
    public ApiResponse<Void> saveIndicatorData(@Valid @RequestBody BatchValueRequest body, HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        mutationService.saveIndicatorData(user, body);
        logClient.operation(TraceContext.getTraceId(request), user, "basic.indicators", "BATCH_SAVE", "批量保存指标数据",
                "basic_indicator_value", null, request, true, "indicator data saved");
        return ApiResponse.ok(null, TraceContext.getTraceId(request));
    }

    @GetMapping("/shifts")
    public ApiResponse<PageResult> shifts(@RequestParam(name = "keyword", required = false) String keyword,
                                          @RequestParam(name = "page", defaultValue = "1") int page,
                                          @RequestParam(name = "size", defaultValue = "10") int size,
                                          HttpServletRequest request) {
        return page("basic.shifts", keyword, page, size, request);
    }

    @PostMapping("/shifts")
    public ApiResponse<Map<String, Object>> createShift(@Valid @RequestBody ShiftRequest body,
                                                        HttpServletRequest request) {
        CurrentUser user = AuthContext.requireUser();
        Map<String, Object> result = mutationService.createShift(user, body);
        logClient.operation(TraceContext.getTraceId(request), user, "basic.shifts", "CREATE", "新增班次",
                "basic_shift", String.valueOf(result.get("id")), request, true, "shift created");
        return ApiResponse.ok(result, TraceContext.getTraceId(request));
    }

    private ApiResponse<PageResult> page(String resource, String keyword, int page, int size, HttpServletRequest request) {
        return ApiResponse.ok(resourceService.page(resource, AuthContext.requireUser(), keyword, page, size),
                TraceContext.getTraceId(request));
    }
}
