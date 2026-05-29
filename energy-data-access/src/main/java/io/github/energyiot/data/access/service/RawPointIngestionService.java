package io.github.energyiot.data.access.service;

import io.github.energyiot.data.access.cleaning.CleaningEvent;
import io.github.energyiot.data.access.cleaning.CleaningEventPublisher;
import io.github.energyiot.data.access.protocol.UnifiedPayload;
import io.github.energyiot.data.access.protocol.UnifiedPayloadDecoder;
import io.github.energyiot.data.access.raw.RawPointRecord;
import io.github.energyiot.data.access.storage.RawPointStorage;
import io.github.energyiot.data.access.storage.RawPointWriteResult;

import java.util.ArrayList;
import java.util.List;

public class RawPointIngestionService {

    private final UnifiedPayloadDecoder decoder;

    private final RawPointStorage storage;

    private final CleaningEventPublisher cleaningEventPublisher;

    public RawPointIngestionService(UnifiedPayloadDecoder decoder,
                                    RawPointStorage storage,
                                    CleaningEventPublisher cleaningEventPublisher) {
        this.decoder = decoder;
        this.storage = storage;
        this.cleaningEventPublisher = cleaningEventPublisher;
    }

    public RawPointWriteResult ingest(UnifiedPayload payload) {
        List<RawPointRecord> records = decoder.decode(payload);
        RawPointWriteResult result = storage.save(records);
        cleaningEventPublisher.publish(toCleaningEvent(result));
        return result;
    }

    private static CleaningEvent toCleaningEvent(RawPointWriteResult result) {
        List<RawPointRecord> records = result.getRecords();
        RawPointRecord first = records.get(0);
        List<String> ids = new ArrayList<>();
        for (RawPointRecord record : records) {
            ids.add(record.getId());
        }
        return new CleaningEvent()
                .setTableName(result.getTableName())
                .setRawIds(ids)
                .setTenantMark(first.getTenantMark())
                .setModelMark(first.getModelMark())
                .setDeviceMark(first.getDeviceMark())
                .setMessageId(first.getMessageId());
    }
}
