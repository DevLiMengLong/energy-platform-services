package io.github.energyiot.data.access.configuration;

import io.github.energyiot.data.access.cleaning.CleanPointConfigProvider;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class CleanPointConfigServiceTest {

    @Test
    void requiresDeviceMarkWhenSavingConfig() {
        CleanPointConfigService service = service();
        CleanPointConfigSaveRequest request = validRequest();
        request.setDeviceMark("");

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deviceMark");
    }

    @Test
    void rejectsMinGreaterThanMax() {
        CleanPointConfigService service = service();
        CleanPointConfigSaveRequest request = validRequest();
        request.setMinValue("100");
        request.setMaxValue("10");

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minValue");
    }

    @Test
    void requiresRolloverThresholdsWhenRolloverEnabled() {
        CleanPointConfigService service = service();
        CleanPointConfigSaveRequest request = validRequest();
        request.setRolloverEnabled(true);
        request.setRolloverMaxValue("999999");

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rolloverMinPreviousValue");
    }

    @Test
    void rejectsUnsupportedFormulaCharacters() {
        CleanPointConfigService service = service();
        CleanPointConfigSaveRequest request = validRequest();
        request.setTransformFormula("Math.max(x, 0)");

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("transformFormula");
    }

    private static CleanPointConfigService service() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        CleanPointConfigProvider provider = mock(CleanPointConfigProvider.class);
        BasicCollectionPointClient client = mock(BasicCollectionPointClient.class);
        return new CleanPointConfigService(jdbcTemplate, "point_clean_config", provider, client);
    }

    private static CleanPointConfigSaveRequest validRequest() {
        CleanPointConfigSaveRequest request = new CleanPointConfigSaveRequest();
        request.setTenantMark("tenant_1");
        request.setModelMark("electric");
        request.setDeviceMark("em_001");
        request.setParamMark("kwh_total");
        request.setTransformFormula("x");
        return request;
    }
}
