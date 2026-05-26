package com.getech.energy.platformbasic.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

public final class TraceContext {

    public static final String ATTRIBUTE = "TRACE_ID";
    public static final String HEADER = "X-Trace-Id";

    private TraceContext() {
    }

    public static String getTraceId(HttpServletRequest request) {
        Object value = request.getAttribute(ATTRIBUTE);
        if (value instanceof String traceId && !traceId.isBlank()) {
            return traceId;
        }
        return UUID.randomUUID().toString();
    }
}
