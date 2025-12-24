package kosukeroku.token_radar.service.kafka;

import kosukeroku.token_radar.dto.kafka.AlertTriggeredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "${app.kafka.topics.alert-triggered}")
    public void consumeAlertTriggered(AlertTriggeredEvent event) {
        try {
            log.info("Kafka alert received: {} for user: {}",
                    event.getCoinName(), event.getUsername());

            messagingTemplate.convertAndSendToUser(
                    event.getUsername(),
                    "/queue/alerts",
                    event
            );

            log.info("WebSocket alert sent to user: {}", event.getUsername());

        } catch (Exception e) {
            log.error("Failed to process alert {}: {}", event.getAlertId(), e.getMessage());
        }
    }
}