package io.github.energyiot.data.access.adapter.http;

import io.github.energyiot.data.access.aggregation.AggregateRecomputeRequest;
import io.github.energyiot.data.access.aggregation.AggregateRecomputeResult;
import io.github.energyiot.data.access.aggregation.AggregateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aggregate")
public class AggregateRecomputeController {

    private final AggregateService aggregateService;

    public AggregateRecomputeController(AggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @PostMapping("/recompute")
    public ResponseEntity<AggregateRecomputeResult> recompute(@RequestBody AggregateRecomputeRequest request) {
        return ResponseEntity.ok(aggregateService.recompute(request));
    }
}
