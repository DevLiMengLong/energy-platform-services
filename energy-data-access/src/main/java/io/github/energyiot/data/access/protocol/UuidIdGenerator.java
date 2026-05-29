package io.github.energyiot.data.access.protocol;

import java.util.UUID;

public class UuidIdGenerator implements IdGenerator {

    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }
}
