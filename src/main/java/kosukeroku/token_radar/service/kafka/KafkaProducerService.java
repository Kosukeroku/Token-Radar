package kosukeroku.token_radar.service.kafka;

import kosukeroku.token_radar.dto.kafka.AlertTriggeredEvent;
import kosukeroku.token_radar.model.PriceAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.alert-triggered}")
    private String alertTriggeredTopic;

    public void sendAlertTriggered(PriceAlert alert) {
        try {
            // creating an event from an alert
            AlertTriggeredEvent event = AlertTriggeredEvent.builder()
                    .alertId(alert.getId())
                    .userId(alert.getUser().getId())
                    .username(alert.getUser().getUsername())
                    .coinId(alert.getCoin().getId())
                    .coinName(alert.getCoin().getName())
                    .coinSymbol(alert.getCoin().getSymbol())
                    .alertType(alert.getType().name())
                    .thresholdValue(alert.getThresholdValue())
                    .triggeredPrice(alert.getTriggeredPrice())
                    .notificationMessage(alert.getNotificationMessage())
                    .triggeredAt(alert.getTriggeredAt())
                    .build();

            // using userId as a key so for every user their alerts are in one partition
            kafkaTemplate.send(alertTriggeredTopic, alert.getUser().getId().toString(), event);

            log.info("Alert {} sent to Kafka for user {}",
                    alert.getId(), alert.getUser().getId());

        } catch (Exception e) {
            log.error("Error sending alert to Kafka: {}", e.getMessage(), e);
        }
    }
}