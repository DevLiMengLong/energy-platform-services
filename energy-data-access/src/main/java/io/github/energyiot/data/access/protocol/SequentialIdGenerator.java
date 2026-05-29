package io.github.energyiot.data.access.protocol;

import java.util.concurrent.atomic.AtomicLong;

public class SequentialIdGenerator implements IdGenerator {

    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public String nextId() {
        return "raw-" + counter.incrementAndGet();
    }
}
