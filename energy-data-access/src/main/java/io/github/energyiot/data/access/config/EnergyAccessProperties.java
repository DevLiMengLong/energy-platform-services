package io.github.energyiot.data.access.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "energy.access")
public class EnergyAccessProperties {

    private String internalRawTopic = "energy.raw.ingest";

    private String cleanPendingTopic = "energy.clean.pending";

    private String aggregatePendingTopic = "energy.aggregate.pending";

    private Cleaning cleaning = new Cleaning();

    private Aggregation aggregation = new Aggregation();

    private Latest latest = new Latest();

    private Storage storage = new Storage();

    private BasicService basicService = new BasicService();

    private ExternalKafka externalKafka = new ExternalKafka();

    private ExternalMqtt externalMqtt = new ExternalMqtt();

    public String getInternalRawTopic() {
        return internalRawTopic;
    }

    public void setInternalRawTopic(String internalRawTopic) {
        this.internalRawTopic = internalRawTopic;
    }

    public String getCleanPendingTopic() {
        return cleanPendingTopic;
    }

    public void setCleanPendingTopic(String cleanPendingTopic) {
        this.cleanPendingTopic = cleanPendingTopic;
    }

    public String getAggregatePendingTopic() {
        return aggregatePendingTopic;
    }

    public void setAggregatePendingTopic(String aggregatePendingTopic) {
        this.aggregatePendingTopic = aggregatePendingTopic;
    }

    public Cleaning getCleaning() {
        return cleaning;
    }

    public void setCleaning(Cleaning cleaning) {
        this.cleaning = cleaning;
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    public void setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
    }

    public Latest getLatest() {
        return latest;
    }

    public void setLatest(Latest latest) {
        this.latest = latest;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public BasicService getBasicService() {
        return basicService;
    }

    public void setBasicService(BasicService basicService) {
        this.basicService = basicService;
    }

    public ExternalKafka getExternalKafka() {
        return externalKafka;
    }

    public void setExternalKafka(ExternalKafka externalKafka) {
        this.externalKafka = externalKafka;
    }

    public ExternalMqtt getExternalMqtt() {
        return externalMqtt;
    }

    public void setExternalMqtt(ExternalMqtt externalMqtt) {
        this.externalMqtt = externalMqtt;
    }

    public static class Storage {
        private String type = "clickhouse";
        private boolean autoCreateTable = true;
        private String tablePrefix = "raw_param";

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isAutoCreateTable() {
            return autoCreateTable;
        }

        public void setAutoCreateTable(boolean autoCreateTable) {
            this.autoCreateTable = autoCreateTable;
        }

        public String getTablePrefix() {
            return tablePrefix;
        }

        public void setTablePrefix(String tablePrefix) {
            this.tablePrefix = tablePrefix;
        }
    }

    public static class Cleaning {
        private long configRefreshMs = 240000;
        private String cleanTablePrefix = "clean_param";
        private String configTable = "point_clean_config";
        private String exportOutputDir = "data/clean-export";

        public long getConfigRefreshMs() {
            return configRefreshMs;
        }

        public void setConfigRefreshMs(long configRefreshMs) {
            this.configRefreshMs = configRefreshMs;
        }

        public String getCleanTablePrefix() {
            return cleanTablePrefix;
        }

        public void setCleanTablePrefix(String cleanTablePrefix) {
            this.cleanTablePrefix = cleanTablePrefix;
        }

        public String getConfigTable() {
            return configTable;
        }

        public void setConfigTable(String configTable) {
            this.configTable = configTable;
        }

        public String getExportOutputDir() {
            return exportOutputDir;
        }

        public void setExportOutputDir(String exportOutputDir) {
            this.exportOutputDir = exportOutputDir;
        }
    }

    public static class BasicService {
        private String baseUrl = "http://127.0.0.1:8090/api/basic";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Aggregation {
        private long minuteDelaySeconds = 90;
        private long fifteenMinuteDelaySeconds = 180;
        private long hourDelaySeconds = 300;
        private long dayDelaySeconds = 600;

        public long getMinuteDelaySeconds() {
            return minuteDelaySeconds;
        }

        public void setMinuteDelaySeconds(long minuteDelaySeconds) {
            this.minuteDelaySeconds = minuteDelaySeconds;
        }

        public long getFifteenMinuteDelaySeconds() {
            return fifteenMinuteDelaySeconds;
        }

        public void setFifteenMinuteDelaySeconds(long fifteenMinuteDelaySeconds) {
            this.fifteenMinuteDelaySeconds = fifteenMinuteDelaySeconds;
        }

        public long getHourDelaySeconds() {
            return hourDelaySeconds;
        }

        public void setHourDelaySeconds(long hourDelaySeconds) {
            this.hourDelaySeconds = hourDelaySeconds;
        }

        public long getDayDelaySeconds() {
            return dayDelaySeconds;
        }

        public void setDayDelaySeconds(long dayDelaySeconds) {
            this.dayDelaySeconds = dayDelaySeconds;
        }
    }

    public static class Latest {
        private boolean redisEnabled = false;
        private long ttlSeconds = 0;

        public boolean isRedisEnabled() {
            return redisEnabled;
        }

        public void setRedisEnabled(boolean redisEnabled) {
            this.redisEnabled = redisEnabled;
        }

        public long getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(long ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }
    }

    public static class ExternalKafka {
        private String topic = "energy.external.ingest";

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }

    public static class ExternalMqtt {
        private boolean enabled = false;
        private String brokerUrl = "tcp://localhost:1883";
        private String clientId = "energy-data-access";
        private String topic = "energy/+/+/+/telemetry";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBrokerUrl() {
            return brokerUrl;
        }

        public void setBrokerUrl(String brokerUrl) {
            this.brokerUrl = brokerUrl;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }
}
