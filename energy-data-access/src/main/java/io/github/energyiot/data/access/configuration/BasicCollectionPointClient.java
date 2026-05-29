package io.github.energyiot.data.access.configuration;

import io.github.energyiot.data.access.config.EnergyAccessProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BasicCollectionPointClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public BasicCollectionPointClient(RestTemplate restTemplate, EnergyAccessProperties.BasicService properties) {
        this.restTemplate = restTemplate;
        this.baseUrl = trimTrailingSlash(properties.getBaseUrl());
    }

    public BasicPointPage page(CleanPointConfigPageRequest request, String authorization) {
        if (!StringUtils.hasText(baseUrl)) {
            return new BasicPointPage(0, Collections.emptyList());
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/collection-points")
                .queryParam("page", 1)
                .queryParam("size", 200);
        append(builder, "keyword", request.getPointName());
        append(builder, "collectionModelMark", request.getModelMark());
        append(builder, "collectionDeviceMark", request.getDeviceMark());
        append(builder, "collectionParamMark", request.getParamMark());
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(authorization)) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        try {
            URI uri = builder.build(true).toUri();
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<Void>(headers), Map.class);
            Object body = response.getBody();
            Object data = body instanceof Map ? ((Map<?, ?>) body).get("data") : null;
            if (!(data instanceof Map)) {
                return new BasicPointPage(0, Collections.emptyList());
            }
            Map<?, ?> page = (Map<?, ?>) data;
            Object rowsValue = page.get("rows");
            List<Map<String, Object>> rows = new ArrayList<>();
            if (rowsValue instanceof List) {
                for (Object item : (List<?>) rowsValue) {
                    if (item instanceof Map) {
                        rows.add(new LinkedHashMap<>((Map<String, Object>) item));
                    }
                }
            }
            return new BasicPointPage(number(page.get("total")), rows);
        } catch (Exception ignored) {
            return new BasicPointPage(0, Collections.emptyList());
        }
    }

    private static void append(UriComponentsBuilder builder, String name, String value) {
        if (StringUtils.hasText(value)) {
            builder.queryParam(name, value);
        }
    }

    private static long number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public static class BasicPointPage {
        private final long total;
        private final List<Map<String, Object>> rows;

        BasicPointPage(long total, List<Map<String, Object>> rows) {
            this.total = total;
            this.rows = rows;
        }

        public long getTotal() {
            return total;
        }

        public List<Map<String, Object>> getRows() {
            return rows;
        }
    }
}
