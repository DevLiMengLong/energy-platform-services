# Testing

## Unit verification

Run all unit tests:

```bash
mvn test
```

Current coverage focuses on:

- Unified protocol decoding.
- Kafka key generation: `tenantMark|modelMark|deviceMark`.
- Raw table naming and ClickHouse/TDengine DDL generation.
- Raw point persistence without deduplication.
- Cleaning event publication only after raw storage succeeds.
- Internal Kafka publishing to the unified raw topic.

## Reliability check

The reliability script sends the same telemetry message multiple times. The expected behavior is that every upload is accepted and stored; duplicate business payloads are not deduplicated.

```bash
BASE_URL=http://127.0.0.1:8088 REPEAT=3 scripts/http_reliability_check.sh
```

## Performance check

The performance script sends configurable concurrent HTTP telemetry requests and prints success count, QPS, average latency, and p50/p95/p99 latency.

```bash
BASE_URL=http://127.0.0.1:8088 TOTAL=10000 CONCURRENCY=50 scripts/http_performance_check.sh
```

Use the result as a baseline. Kafka, storage engine, table partitioning, and server resource limits should be tuned before treating the number as a capacity target.
