package com.getech.energy.platformbasic.common;

import java.util.List;
import java.util.Map;

public record PageResult(long total, int page, int size, List<Map<String, Object>> rows) {
}
