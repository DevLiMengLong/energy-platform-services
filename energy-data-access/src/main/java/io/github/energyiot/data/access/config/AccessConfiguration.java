package io.github.energyiot.data.access.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.energyiot.data.access.aggregation.AggregateCalculator;
import io.github.energyiot.data.access.aggregation.AggregateRepository;
import io.github.energyiot.data.access.aggregation.AggregateScheduler;
import io.github.energyiot.data.access.aggregation.AggregateService;
import io.github.energyiot.data.access.aggregation.JdbcAggregateRepository;
import io.github.energyiot.data.access.cleaning.AggregateEventPublisher;
import io.github.energyiot.data.access.cleaning.CleanPointConfigProvider;
import io.github.energyiot.data.access.cleaning.CleanPointProcessor;
import io.github.energyiot.data.access.cleaning.CleanPointRepository;
import io.github.energyiot.data.access.cleaning.CleaningEventPublisher;
import io.github.energyiot.data.access.cleaning.FormulaEvaluator;
import io.github.energyiot.data.access.cleaning.JdbcCleanPointConfigProvider;
import io.github.energyiot.data.access.cleaning.JdbcCleanPointRepository;
import io.github.energyiot.data.access.cleaning.KafkaAggregateEventPublisher;
import io.github.energyiot.data.access.cleaning.KafkaCleaningEventPublisher;
import io.github.energyiot.data.access.configuration.BasicCollectionPointClient;
import io.github.energyiot.data.access.configuration.CleanPointConfigService;
import io.github.energyiot.data.access.ingress.InternalRawKafkaPublisher;
import io.github.energyiot.data.access.ingress.InternalRawPublisher;
import io.github.energyiot.data.access.ingress.KafkaSender;
import io.github.energyiot.data.access.ingress.KafkaTemplateSender;
import io.github.energyiot.data.access.latest.LatestCleanPointStore;
import io.github.energyiot.data.access.latest.NoopLatestCleanPointStore;
import io.github.energyiot.data.access.latest.RedisLatestCleanPointStore;
import io.github.energyiot.data.access.protocol.IdGenerator;
import io.github.energyiot.data.access.protocol.UnifiedPayloadDecoder;
import io.github.energyiot.data.access.protocol.UuidIdGenerator;
import io.github.energyiot.data.access.query.CleanExportTaskService;
import io.github.energyiot.data.access.query.PointQueryService;
import io.github.energyiot.data.access.service.RawPointIngestionService;
import io.github.energyiot.data.access.storage.ClickHouseRawPointDdlFactory;
import io.github.energyiot.data.access.storage.ClickHouseRawPointInsertFactory;
import io.github.energyiot.data.access.storage.JdbcRawPointStorage;
import io.github.energyiot.data.access.storage.JdbcTemplateRawPointOperations;
import io.github.energyiot.data.access.storage.RawPointDdlFactory;
import io.github.energyiot.data.access.storage.RawPointInsertFactory;
import io.github.energyiot.data.access.storage.RawPointStorage;
import io.github.energyiot.data.access.storage.RawPointTableNameResolver;
import io.github.energyiot.data.access.storage.TdengineRawPointDdlFactory;
import io.github.energyiot.data.access.storage.TdengineRawPointInsertFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.nio.file.Paths;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(EnergyAccessProperties.class)
public class AccessConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    IdGenerator idGenerator() {
        return new UuidIdGenerator();
    }

    @Bean
    UnifiedPayloadDecoder unifiedPayloadDecoder(IdGenerator idGenerator, Clock clock) {
        return new UnifiedPayloadDecoder(idGenerator, clock);
    }

    @Bean
    KafkaSender kafkaSender(KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaTemplateSender(kafkaTemplate);
    }

    @Bean
    InternalRawPublisher internalRawPublisher(KafkaSender kafkaSender,
                                             ObjectMapper objectMapper,
                                             EnergyAccessProperties properties) {
        return new InternalRawKafkaPublisher(kafkaSender, objectMapper, properties.getInternalRawTopic());
    }

    @Bean
    CleaningEventPublisher cleaningEventPublisher(KafkaSender kafkaSender,
                                                  ObjectMapper objectMapper,
                                                  EnergyAccessProperties properties) {
        return new KafkaCleaningEventPublisher(kafkaSender, objectMapper, properties.getCleanPendingTopic());
    }

    @Bean
    AggregateEventPublisher aggregateEventPublisher(KafkaSender kafkaSender,
                                                    ObjectMapper objectMapper,
                                                    EnergyAccessProperties properties) {
        return new KafkaAggregateEventPublisher(kafkaSender, objectMapper, properties.getAggregatePendingTopic());
    }

    @Bean
    FormulaEvaluator formulaEvaluator() {
        return new FormulaEvaluator();
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    CleanPointConfigProvider cleanPointConfigProvider(JdbcTemplate jdbcTemplate,
                                                      EnergyAccessProperties properties) {
        return new JdbcCleanPointConfigProvider(jdbcTemplate, properties.getCleaning().getConfigTable());
    }

    @Bean
    CleanPointRepository cleanPointRepository(JdbcTemplate jdbcTemplate,
                                              EnergyAccessProperties properties) {
        return new JdbcCleanPointRepository(jdbcTemplate, properties.getStorage().isAutoCreateTable());
    }

    @Bean
    CleanPointProcessor cleanPointProcessor(IdGenerator idGenerator,
                                            FormulaEvaluator formulaEvaluator,
                                            CleanPointConfigProvider configProvider,
                                            CleanPointRepository repository,
                                            AggregateEventPublisher aggregateEventPublisher,
                                            LatestCleanPointStore latestCleanPointStore,
                                            Clock clock,
                                            EnergyAccessProperties properties) {
        return new CleanPointProcessor(
                idGenerator,
                formulaEvaluator,
                configProvider,
                repository,
                aggregateEventPublisher,
                latestCleanPointStore,
                clock,
                properties.getCleaning().getCleanTablePrefix()
        );
    }

    @Bean
    LatestCleanPointStore latestCleanPointStore(StringRedisTemplate redisTemplate,
                                                ObjectMapper objectMapper,
                                                EnergyAccessProperties properties) {
        if (properties.getLatest().isRedisEnabled()) {
            return new RedisLatestCleanPointStore(redisTemplate, objectMapper, properties.getLatest().getTtlSeconds());
        }
        return new NoopLatestCleanPointStore();
    }

    @Bean
    CleanPointConfigService cleanPointConfigService(JdbcTemplate jdbcTemplate,
                                                    CleanPointConfigProvider configProvider,
                                                    BasicCollectionPointClient basicCollectionPointClient,
                                                    EnergyAccessProperties properties) {
        return new CleanPointConfigService(jdbcTemplate, properties.getCleaning().getConfigTable(), configProvider, basicCollectionPointClient);
    }

    @Bean
    BasicCollectionPointClient basicCollectionPointClient(RestTemplate restTemplate,
                                                         EnergyAccessProperties properties) {
        return new BasicCollectionPointClient(restTemplate, properties.getBasicService());
    }

    @Bean
    PointQueryService pointQueryService(JdbcTemplate jdbcTemplate,
                                        LatestCleanPointStore latestCleanPointStore,
                                        EnergyAccessProperties properties) {
        return new PointQueryService(
                jdbcTemplate,
                latestCleanPointStore,
                properties.getStorage().getTablePrefix(),
                properties.getCleaning().getCleanTablePrefix()
        );
    }

    @Bean
    CleanExportTaskService cleanExportTaskService(EnergyAccessProperties properties) {
        return new CleanExportTaskService(Paths.get(properties.getCleaning().getExportOutputDir()));
    }

    @Bean
    AggregateCalculator aggregateCalculator(IdGenerator idGenerator,
                                            Clock clock,
                                            FormulaEvaluator formulaEvaluator) {
        return new AggregateCalculator(idGenerator, clock, formulaEvaluator);
    }

    @Bean
    AggregateRepository aggregateRepository(JdbcTemplate jdbcTemplate,
                                            EnergyAccessProperties properties) {
        return new JdbcAggregateRepository(jdbcTemplate, properties.getStorage().isAutoCreateTable());
    }

    @Bean
    AggregateService aggregateService(AggregateRepository aggregateRepository,
                                      AggregateCalculator aggregateCalculator,
                                      CleanPointConfigProvider configProvider,
                                      EnergyAccessProperties properties) {
        return new AggregateService(
                aggregateRepository,
                aggregateCalculator,
                configProvider,
                properties.getCleaning().getCleanTablePrefix()
        );
    }

    @Bean
    AggregateScheduler aggregateScheduler(AggregateService aggregateService,
                                          Clock clock,
                                          EnergyAccessProperties properties) {
        EnergyAccessProperties.Aggregation aggregation = properties.getAggregation();
        return new AggregateScheduler(
                aggregateService,
                clock,
                aggregation.getMinuteDelaySeconds(),
                aggregation.getFifteenMinuteDelaySeconds(),
                aggregation.getHourDelaySeconds(),
                aggregation.getDayDelaySeconds()
        );
    }

    @Bean
    RawPointStorage rawPointStorage(JdbcTemplate jdbcTemplate, EnergyAccessProperties properties) {
        RawPointDdlFactory ddlFactory;
        RawPointInsertFactory insertFactory;
        if ("tdengine".equalsIgnoreCase(properties.getStorage().getType())) {
            ddlFactory = new TdengineRawPointDdlFactory();
            insertFactory = new TdengineRawPointInsertFactory();
        } else {
            ddlFactory = new ClickHouseRawPointDdlFactory();
            insertFactory = new ClickHouseRawPointInsertFactory();
        }
        return new JdbcRawPointStorage(
                new JdbcTemplateRawPointOperations(jdbcTemplate),
                new RawPointTableNameResolver(properties.getStorage().getTablePrefix()),
                ddlFactory,
                insertFactory,
                properties.getStorage().isAutoCreateTable()
        );
    }

    @Bean
    RawPointIngestionService rawPointIngestionService(UnifiedPayloadDecoder decoder,
                                                      RawPointStorage storage,
                                                      CleaningEventPublisher cleaningEventPublisher) {
        return new RawPointIngestionService(decoder, storage, cleaningEventPublisher);
    }
}
