package io.github.energyiot.data.access.adapter.http;

import io.github.energyiot.data.access.query.CleanExportRequest;
import io.github.energyiot.data.access.query.CleanExportTaskService;
import io.github.energyiot.data.access.latest.LatestCleanPoint;
import io.github.energyiot.data.access.query.PageResult;
import io.github.energyiot.data.access.query.PointQueryRequest;
import io.github.energyiot.data.access.query.PointQueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/query")
public class PointQueryController {
    private final PointQueryService queryService;
    private final CleanExportTaskService exportTaskService;

    public PointQueryController(PointQueryService queryService, CleanExportTaskService exportTaskService) {
        this.queryService = queryService;
        this.exportTaskService = exportTaskService;
    }

    @PostMapping("/raw/points")
    public PageResult rawPoints(@RequestBody PointQueryRequest request) {
        return queryService.rawPoints(request);
    }

    @PostMapping("/clean/points")
    public PageResult cleanPoints(@RequestBody PointQueryRequest request) {
        return queryService.cleanPoints(request);
    }

    @PostMapping("/clean/export")
    public Map<String, Object> cleanExport(@RequestBody CleanExportRequest request) {
        return exportTaskService.create(request);
    }

    @PostMapping("/clean/latest")
    public List<LatestCleanPoint> latest(@RequestBody PointQueryRequest request) {
        return queryService.latest(request);
    }

    @PostMapping("/aggregate/points")
    public PageResult aggregatePoints(@RequestBody PointQueryRequest request) {
        return queryService.aggregatePoints(request);
    }
}
