package io.github.energyiot.data.access.adapter.http;

import io.github.energyiot.data.access.configuration.CleanPointConfigQueryRequest;
import io.github.energyiot.data.access.configuration.CleanPointConfigPageRequest;
import io.github.energyiot.data.access.configuration.CleanPointConfigSaveRequest;
import io.github.energyiot.data.access.configuration.CleanPointConfigService;
import io.github.energyiot.data.access.query.PageResult;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config/clean-point")
public class CleanPointConfigController {
    private final CleanPointConfigService service;

    public CleanPointConfigController(CleanPointConfigService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody CleanPointConfigSaveRequest request) {
        service.save(request);
        return java.util.Collections.singletonMap("success", true);
    }

    @PostMapping("/list")
    public List<Map<String, Object>> list(@RequestBody CleanPointConfigQueryRequest request) {
        return service.list(request);
    }

    @PostMapping("/page")
    public PageResult page(@RequestBody CleanPointConfigPageRequest request,
                           @RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.page(request, authorization);
    }

    @PostMapping("/disable")
    public Map<String, Object> disable(@RequestBody CleanPointConfigQueryRequest request) {
        service.disable(request);
        return java.util.Collections.singletonMap("success", true);
    }

    @PostMapping("/reload")
    public Map<String, Object> reload() {
        service.reload();
        return java.util.Collections.singletonMap("success", true);
    }
}
