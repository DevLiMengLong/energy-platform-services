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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformBasicApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginReturnsTokenAndTenantMenus() throws Exception {
        String token = login("tenant_a_admin", "admin123");

        mockMvc.perform(get("/api/basic/me/menus").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].code").value("basic"))
                .andExpect(jsonPath("$.data[0].menus[0].nameZh").value("组织管理"));
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
