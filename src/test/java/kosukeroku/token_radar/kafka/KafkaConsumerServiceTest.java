package kosukeroku.token_radar.kafka;

import kosukeroku.token_radar.dto.kafka.AlertTriggeredEvent;
import kosukeroku.token_radar.service.kafka.KafkaConsumerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @Test
    void consumeAlertTriggered_ShouldSendWebSocketMessage() {
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
        kafkaConsumerService.consumeAlertTriggered(event);

        // then
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AlertTriggeredEvent> eventCaptor = ArgumentCaptor.forClass(AlertTriggeredEvent.class);

        verify(messagingTemplate).convertAndSendToUser(
                usernameCaptor.capture(),
                destinationCaptor.capture(),
                eventCaptor.capture()
        );

        assertThat(usernameCaptor.getValue()).isEqualTo("testuser");
        assertThat(destinationCaptor.getValue()).isEqualTo("/queue/alerts");
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    void consumeAlertTriggered_ShouldHandleExceptionGracefully() {
        // given
        AlertTriggeredEvent event = AlertTriggeredEvent.builder()
                .alertId(1L)
                .userId(1L)
                .username("testuser")
                .build();

        doThrow(new RuntimeException("WebSocket error"))
                .when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        // when
        Throwable thrown = catchThrowable(() -> kafkaConsumerService.consumeAlertTriggered(event));

        // then
        assertThat(thrown).isNull();
    }
}