package io.github.energyiot.data.access.ingress;

import io.github.energyiot.data.access.protocol.UnifiedPayload;

public interface InternalRawPublisher {

    void publish(UnifiedPayload payload);
}
