package kosukeroku.token_radar.kafka;

import kosukeroku.token_radar.dto.kafka.AlertTriggeredEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"alert-triggered-topic"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        }
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.kafka.consumer.group-id=test-consumer-group",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*",
        "app.kafka.topics.alert-triggered=alert-triggered-topic"
})
class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, AlertTriggeredEvent> kafkaTemplate;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void shouldSendAndReceiveKafkaMessage() {
        // given
        AlertTriggeredEvent event = AlertTriggeredEvent.builder()
                .alertId(1L)
                .userId(1L)
                .username("testuser")
                .coinId("bitcoin")
                .coinName("Bitcoin")
                .coinSymbol("BTC")
                .alertType("PRICE_ABOVE")
                .thresholdValue(BigDecimal.valueOf(60000))
                .triggeredPrice(BigDecimal.valueOf(61000))
                .notificationMessage("Price reached target!")
                .triggeredAt(LocalDateTime.now())
                .build();

        // when
        kafkaTemplate.send("alert-triggered-topic", "1", event);

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(messagingTemplate, timeout(5000)).convertAndSendToUser(
                    eq("testuser"),
                    eq("/queue/alerts"),
                    eq(event)
            );
        });
    }
}