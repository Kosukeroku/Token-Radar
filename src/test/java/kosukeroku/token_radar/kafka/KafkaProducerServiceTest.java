package kosukeroku.token_radar.kafka;

import kosukeroku.token_radar.dto.kafka.AlertTriggeredEvent;
import kosukeroku.token_radar.model.PriceAlert;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.model.enums.AlertType;
import kosukeroku.token_radar.model.enums.AlertStatus;
import kosukeroku.token_radar.service.kafka.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaProducerService, "alertTriggeredTopic", "alert-triggered-topic");
    }

    @Test
    void sendAlertTriggered_ShouldSendEventToKafka() {
        // given
        PriceAlert alert = createTestAlert();

        // when
        kafkaProducerService.sendAlertTriggered(alert);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AlertTriggeredEvent> eventCaptor = ArgumentCaptor.forClass(AlertTriggeredEvent.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("alert-triggered-topic");
        assertThat(keyCaptor.getValue()).isEqualTo("1");
        assertThat(eventCaptor.getValue().getAlertId()).isEqualTo(1L);
        assertThat(eventCaptor.getValue().getUsername()).isEqualTo("testuser");
    }

    @Test
    void sendAlertTriggered_ShouldLogErrorOnException() {
        // given
        PriceAlert alert = createTestAlert();

        doThrow(new RuntimeException("Kafka error"))
                .when(kafkaTemplate).send(eq("alert-triggered-topic"), eq("1"), any(AlertTriggeredEvent.class));

        // when
        Throwable thrown = catchThrowable(() -> kafkaProducerService.sendAlertTriggered(alert));

        // then
        assertThat(thrown).isNull();
        verify(kafkaTemplate).send(eq("alert-triggered-topic"), eq("1"), any(AlertTriggeredEvent.class));
    }

    @Test
    void sendAlertTriggered_ShouldHandleNullAlertValues() {
        // given
        PriceAlert alert = createTestAlert();
        alert.setTriggeredPrice(null);
        alert.setTriggeredAt(null);
        alert.setNotificationMessage(null);

        // when
        kafkaProducerService.sendAlertTriggered(alert);

        // then
        verify(kafkaTemplate).send(eq("alert-triggered-topic"), eq("1"), any(AlertTriggeredEvent.class));
    }

    @Test
    void sendAlertTriggered_ShouldUseCorrectPartitionKey() {
        // given
        PriceAlert alert = createTestAlert();
        alert.getUser().setId(999L);

        // when
        kafkaProducerService.sendAlertTriggered(alert);

        // then
        verify(kafkaTemplate).send(eq("alert-triggered-topic"), eq("999"), any(AlertTriggeredEvent.class));
    }

    private PriceAlert createTestAlert() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        Coin coin = new Coin();
        coin.setId("bitcoin");
        coin.setName("Bitcoin");
        coin.setSymbol("BTC");

        PriceAlert alert = new PriceAlert();
        alert.setId(1L);
        alert.setUser(user);
        alert.setCoin(coin);
        alert.setType(AlertType.PRICE_ABOVE);
        alert.setStatus(AlertStatus.TRIGGERED);
        alert.setThresholdValue(BigDecimal.valueOf(60000));
        alert.setTriggeredPrice(BigDecimal.valueOf(61000));
        alert.setTriggeredAt(LocalDateTime.now());
        alert.setNotificationMessage("Bitcoin reached $61000!");

        return alert;
    }
}