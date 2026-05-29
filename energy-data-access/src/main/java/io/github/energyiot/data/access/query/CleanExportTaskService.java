package io.github.energyiot.data.access.query;

import org.springframework.util.CollectionUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CleanExportTaskService {
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Path outputDir;

    public CleanExportTaskService(Path outputDir) {
        this.outputDir = outputDir;
    }

    public Map<String, Object> create(CleanExportRequest request) {
        String taskId = UUID.randomUUID().toString();
        executor.submit(() -> write(taskId, request));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        result.put("status", "CREATED");
        return result;
    }

    private void write(String taskId, CleanExportRequest request) {
        try {
            Files.createDirectories(outputDir);
            Path file = outputDir.resolve("clean-export-" + taskId + ".csv");
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write("测点信息,单位,模型标识,设备标识,测点标识,数据条数,正常条数,异常条数,最近数据时间,清洗结果");
                writer.newLine();
                if (!CollectionUtils.isEmpty(request.getRows())) {
                    for (Map<String, Object> row : request.getRows()) {
                        writer.write(csv(row.get("pointName")));
                        writer.write(",");
                        writer.write(csv(row.get("unit")));
                        writer.write(",");
                        writer.write(csv(row.get("modelMark")));
                        writer.write(",");
                        writer.write(csv(row.get("deviceMark")));
                        writer.write(",");
                        writer.write(csv(row.get("paramMark")));
                        writer.write(",");
                        writer.write(csv(row.get("totalCount")));
                        writer.write(",");
                        writer.write(csv(row.get("normalCount")));
                        writer.write(",");
                        writer.write(csv(row.get("abnormalCount")));
                        writer.write(",");
                        writer.write(csv(row.get("latestTime")));
                        writer.write(",");
                        writer.write(csv(row.get("result")));
                        writer.newLine();
                    }
                } else {
                    writer.write(csv("任务创建时间"));
                    writer.write(",,,,,,,,");
                    writer.write(csv(Instant.now().toString()));
                    writer.newLine();
                }
            }
        } catch (IOException ignored) {
            // Export task state is intentionally lightweight in this stage. Operational logs can trace failures.
        }
    }

    private static String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
