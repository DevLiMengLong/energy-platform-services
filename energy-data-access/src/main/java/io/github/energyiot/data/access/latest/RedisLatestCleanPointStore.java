package io.github.energyiot.data.access.latest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.energyiot.data.access.cleaning.CleanPointRecord;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;

public class RedisLatestCleanPointStore implements LatestCleanPointStore {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final long ttlSeconds;

    public RedisLatestCleanPointStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public void update(CleanPointRecord record) {
        String key = key(record.getTenantMark(), record.getModelMark(), record.getDeviceMark(), record.getParamMark());
        Optional<LatestCleanPoint> existing = get(record.getTenantMark(), record.getModelMark(), record.getDeviceMark(), record.getParamMark());
        if (existing.isPresent() && existing.get().getNormalSecond() > record.getNormalSecond()) {
            return;
        }
        LatestCleanPoint latest = new LatestCleanPoint()
                .setCleanId(record.getId())
                .setRawId(record.getRawId())
                .setTenantMark(record.getTenantMark())
                .setModelMark(record.getModelMark())
                .setDeviceMark(record.getDeviceMark())
                .setParamMark(record.getParamMark())
                .setCleanValue(record.getCleanValue())
                .setQualityCode(record.getQualityCode())
                .setDeviceTime(record.getDeviceTime())
                .setNormalSecond(record.getNormalSecond());
        try {
            String value = objectMapper.writeValueAsString(latest);
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            throw new IllegalStateException("failed to update latest clean point", e);
        }
    }

    @Override
    public Optional<LatestCleanPoint> get(String tenantMark, String modelMark, String deviceMark, String paramMark) {
        String value = redisTemplate.opsForValue().get(key(tenantMark, modelMark, deviceMark, paramMark));
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, LatestCleanPoint.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String key(String tenantMark, String modelMark, String deviceMark, String paramMark) {
        return "latest:" + tenantMark + ":" + modelMark + ":" + deviceMark + ":" + paramMark;
    }
}
