package com.getech.energy.logservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LogServiceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void writesAndReadsLoginAndOperationLogs() throws Exception {
        mockMvc.perform(post("/api/logs/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "traceId": "trace-login-1",
                                  "tenantId": 1,
                                  "userId": 2,
                                  "account": "tenant_a_admin",
                                  "loginStatus": "SUCCESS",
                                  "clientIp": "127.0.0.1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        mockMvc.perform(post("/api/logs/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "traceId": "trace-op-1",
                                  "tenantId": 1,
                                  "userId": 2,
                                  "account": "tenant_a_admin",
                                  "subsystemCode": "platform-basic",
                                  "moduleCode": "basic.energyTypes",
                                  "actionCode": "CREATE",
                                  "actionName": "新增能源类型",
                                  "success": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        mockMvc.perform(get("/api/logs/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].traceId").value("trace-login-1"));

        mockMvc.perform(get("/api/logs/operation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].traceId").value("trace-op-1"));
    }
}
