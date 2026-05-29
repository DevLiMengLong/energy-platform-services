package io.github.energyiot.data.access.protocol;

import io.github.energyiot.data.access.raw.RawPointRecord;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UnifiedPayloadDecoder {

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IdGenerator idGenerator;

    private final Clock clock;

    public UnifiedPayloadDecoder(IdGenerator idGenerator, Clock clock) {
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    public List<RawPointRecord> decode(UnifiedPayload payload) {
        validate(payload);

        Instant receiveTime = Instant.now(clock);
        Instant deviceTime = parseDeviceTime(payload.getTimestamp(), receiveTime);
        List<RawPointRecord> records = new ArrayList<>();

        for (Map.Entry<String, Object> entry : payload.getData().entrySet()) {
            records.add(new RawPointRecord()
                    .setId(idGenerator.nextId())
                    .setMessageId(payload.getMessageId())
                    .setProtocolVersion(payload.getProtocolVersion())
                    .setTenantMark(payload.getTenantMark())
                    .setModelMark(payload.getModelMark())
                    .setDeviceMark(payload.getDeviceMark())
                    .setParamMark(entry.getKey())
                    .setRawValue(toRawValue(entry.getValue()))
                    .setDeviceTime(deviceTime)
                    .setReceiveTime(receiveTime)
                    .setNormalSecond(deviceTime.getEpochSecond())
                    .setCreatedTime(receiveTime));
        }

        return Collections.unmodifiableList(records);
    }

    public static String kafkaKey(UnifiedPayload payload) {
        return payload.getTenantMark() + "|" + payload.getModelMark() + "|" + payload.getDeviceMark();
    }

    private static void validate(UnifiedPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        if (!StringUtils.hasText(payload.getProtocolVersion())) {
            throw new IllegalArgumentException("protocolVersion must not be blank");
        }
        if (!StringUtils.hasText(payload.getTenantMark())) {
            throw new IllegalArgumentException("tenantMark must not be blank");
        }
        if (!StringUtils.hasText(payload.getModelMark())) {
            throw new IllegalArgumentException("modelMark must not be blank");
        }
        if (!StringUtils.hasText(payload.getDeviceMark())) {
            throw new IllegalArgumentException("deviceMark must not be blank");
        }
        if (!StringUtils.hasText(payload.getMessageId())) {
            throw new IllegalArgumentException("messageId must not be blank");
        }
        if (payload.getData() == null || payload.getData().isEmpty()) {
            throw new IllegalArgumentException("data must not be empty");
        }
    }

    private static Instant parseDeviceTime(String timestamp, Instant fallback) {
        if (!StringUtils.hasText(timestamp)) {
            return fallback;
        }
        try {
            return Instant.parse(timestamp);
        } catch (DateTimeParseException ignore) {
            LocalDateTime localDateTime = LocalDateTime.parse(timestamp, LOCAL_DATE_TIME_FORMATTER);
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        }
    }

    private static String toRawValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
