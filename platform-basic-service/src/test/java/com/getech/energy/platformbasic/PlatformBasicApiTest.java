package com.getech.energy.platformbasic;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformBasicApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void loginReturnsTokenAndTenantMenus() throws Exception {
        String token = login("tenant_a_admin", "admin123");

        mockMvc.perform(get("/api/basic/me/menus").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("basic"))
                .andExpect(jsonPath("$.data[0].menus[0].nameZh").value("组织管理"))
                .andExpect(jsonPath("$.data[1].code").value("cleaning"))
                .andExpect(jsonPath("$.data[1].menus[0].nameZh").value("清洗配置"));
    }

    @Test
    void platformAdminMenusOnlyExposePlatformWorkspace() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/basic/me/menus").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].code").value("platform"))
                .andExpect(jsonPath("$.data[0].menus[0].nameZh").value("租户管理"));
    }

    @Test
    void tenantScopedListsDoNotLeakOtherTenantData() throws Exception {
        String token = login("tenant_b_admin", "admin123");

        mockMvc.perform(get("/api/basic/energy-types").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.rows[0].energyName").value("电"));
    }

    @Test
    void platformListsHaveEnoughDemoRowsAndSupportFilters() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/platform/tenants?page=1&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(25)))
                .andExpect(jsonPath("$.data.rows.length()").value(20));

        mockMvc.perform(get("/api/platform/tenants?industry=电子制造&page=1&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.rows[0].industry").value("电子制造"));

        mockMvc.perform(get("/api/platform/menus/tree?subsystemCode=basic")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data[0].children.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/platform/menus/tree?subsystemCode=basic&keyword=用户")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    void tenantListsHaveEnoughDemoRowsAndSupportFieldFilters() throws Exception {
        String token = login("tenant_a_admin", "admin123");

        mockMvc.perform(get("/api/basic/users?page=1&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(25)))
                .andExpect(jsonPath("$.data.rows.length()").value(20));

        mockMvc.perform(get("/api/basic/org-nodes/tree")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data[0].children.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/basic/users?roleName=能源主管&page=1&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.rows[0].roleName").value("能源主管"));

        mockMvc.perform(get("/api/basic/devices?bindingStatus=BOUND&page=1&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.rows[0].bindingStatus").value("BOUND"));
    }

    @Test
    void platformAdminCanCreateTenantWithGeneratedCode() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(post("/api/platform/tenants")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantName": "自动编码租户",
                                  "industry": "制造业",
                                  "contactName": "测试",
                                  "contactPhone": "13800009999"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantMark", startsWith("TENANT_")))
                .andExpect(jsonPath("$.data.tenantName").value("自动编码租户"));
    }

    @Test
    void tenantAdminCanCreateEnergyTypeWithGeneratedCode() throws Exception {
        String token = login("tenant_a_admin", "admin123");

        mockMvc.perform(post("/api/basic/energy-types")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "energyName": "蒸汽",
                                  "energyUnit": "t",
                                  "standardCoalFactor": 0.1286,
                                  "standardCoalUnit": "kgce/t",
                                  "sortOrder": 9,
                                  "remark": "测试新增"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.energyCode", startsWith("ENERGY_")))
                .andExpect(jsonPath("$.data.energyName").value("蒸汽"));
    }

    @Test
    void genericCatalogActionsAreAcceptedForPlatformAndBasicModules() throws Exception {
        String platformToken = login("admin", "admin123");
        mockMvc.perform(post("/api/platform/actions")
                        .header("Authorization", "Bearer " + platformToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode": "platform.tenants",
                                  "actionCode": "exportTenants",
                                  "actionName": "导出租户",
                                  "targetId": 1,
                                  "targetName": "全链路测试工厂"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accepted").value(true))
                .andExpect(jsonPath("$.data.moduleCode").value("platform.tenants"))
                .andExpect(jsonPath("$.data.actionCode").value("exportTenants"));

        String basicToken = login("tenant_a_admin", "admin123");
        mockMvc.perform(post("/api/basic/actions")
                        .header("Authorization", "Bearer " + basicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode": "basic.users",
                                  "actionCode": "disableUsers",
                                  "actionName": "停用用户",
                                  "targetId": 1,
                                  "targetName": "能源主管"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accepted").value(true))
                .andExpect(jsonPath("$.data.moduleCode").value("basic.users"))
                .andExpect(jsonPath("$.data.actionCode").value("disableUsers"));
    }

    @Test
    void statusActionsMutateBusinessRowsAndRefreshableState() throws Exception {
        String basicToken = login("tenant_a_admin", "admin123");

        mockMvc.perform(post("/api/basic/actions")
                        .header("Authorization", "Bearer " + basicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode": "basic.users",
                                  "actionCode": "disableUsers",
                                  "actionName": "停用用户",
                                  "targetId": 10,
                                  "targetName": "能源主管"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accepted").value(true))
                .andExpect(jsonPath("$.data.affectedRows").value(1));
        assertUserStatus(10L, "DISABLED");

        mockMvc.perform(post("/api/basic/actions")
                        .header("Authorization", "Bearer " + basicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode": "basic.users",
                                  "actionCode": "enableUsers",
                                  "actionName": "启用用户",
                                  "targetId": 10,
                                  "targetName": "能源主管"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.affectedRows").value(1));
        assertUserStatus(10L, "ENABLED");

        mockMvc.perform(post("/api/basic/actions")
                        .header("Authorization", "Bearer " + basicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode": "basic.orgNodes",
                                  "actionCode": "disableOrgMember",
                                  "actionName": "停用",
                                  "targetId": 11,
                                  "targetName": "设备维护"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.affectedRows").value(1));
        assertUserStatus(11L, "DISABLED");

        mockMvc.perform(post("/api/basic/actions")
                        .header("Authorization", "Bearer " + basicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode": "basic.orgNodes",
                                  "actionCode": "enableOrgMember",
                                  "actionName": "启用",
                                  "targetId": 11,
                                  "targetName": "设备维护"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.affectedRows").value(1));
        assertUserStatus(11L, "ENABLED");

        String platformToken = login("admin", "admin123");
        mockMvc.perform(post("/api/platform/tenants/1/disable")
                        .header("Authorization", "Bearer " + platformToken))
                .andExpect(status().isOk());
        assertTenantStatus(1L, "DISABLED");

        mockMvc.perform(post("/api/platform/actions")
                        .header("Authorization", "Bearer " + platformToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode": "platform.tenants",
                                  "actionCode": "enableTenant",
                                  "actionName": "启用租户",
                                  "targetId": 1,
                                  "targetName": "全链路测试工厂"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.affectedRows").value(1));
        assertTenantStatus(1L, "ENABLED");
    }

    private void assertUserStatus(Long id, String status) {
        String actual = jdbcClient.sql("SELECT status FROM basic_user WHERE id = :id")
                .param("id", id)
                .query(String.class)
                .single();
        org.assertj.core.api.Assertions.assertThat(actual).isEqualTo(status);
    }

    private void assertTenantStatus(Long id, String status) {
        String actual = jdbcClient.sql("SELECT status FROM basic_tenant WHERE id = :id")
                .param("id", id)
                .query(String.class)
                .single();
        org.assertj.core.api.Assertions.assertThat(actual).isEqualTo(status);
    }

    private String login(String account, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/basic/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "account": "%s",
                                  "password": "%s"
                                }
                                """.formatted(account, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.token");
    }
}
