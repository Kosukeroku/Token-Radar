package kosukeroku.token_radar.service;

import kosukeroku.token_radar.model.PriceAlert;
import kosukeroku.token_radar.model.enums.AlertStatus;
import kosukeroku.token_radar.model.enums.AlertType;
import kosukeroku.token_radar.repository.PriceAlertRepository;
import kosukeroku.token_radar.service.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriceAlertCheckerService {

    private final PriceAlertRepository priceAlertRepository;
    private final KafkaProducerService kafkaProducerService;

    //private static final double BUFFER_ZONE = 0.2; // not using this (for now?)
    private static final String PRICE_ABOVE_ALERT_MESSAGE = "%s reached your price target (%s)! Price: $%s";
    private static final String PRICE_BELOW_ALERT_MESSAGE = "%s dropped below your price alert (%s)! Price: $%s";
    private static final String PERCENTAGE_UP_ALERT_MESSAGE = "%s reached your %% target (%s%%)! Current price: $%s";
    private static final String PERCENTAGE_DOWN_ALERT_MESSAGE = "%s dropped below your %% target (%s%%)! Current price: $%s";

    @Transactional
    public List<PriceAlert> checkAndTriggerAlerts(String coinId, BigDecimal currentPrice) {
        List<PriceAlert> activeAlerts = priceAlertRepository.findActiveAlertsForCoin(coinId);
        List<PriceAlert> triggeredAlerts = new ArrayList<>();

        for (PriceAlert alert : activeAlerts) {
            if (shouldTrigger(alert, currentPrice)) {
                triggerAlert(alert, currentPrice);
                triggeredAlerts.add(alert);
            }
        }

        if (!triggeredAlerts.isEmpty()) {
            // saving all in one batch
            priceAlertRepository.saveAll(triggeredAlerts);
            log.info("Triggered {} alerts for coin {}", triggeredAlerts.size(), coinId);
        }

        return triggeredAlerts;
    }

    private boolean shouldTrigger(PriceAlert alert, BigDecimal currentPrice) {
        if (alert.getStatus() != AlertStatus.ACTIVE) {
            return false;
        }

        alert.setLastCheckedPrice(currentPrice);

        return checkThreshold(alert, currentPrice);
    }

    private boolean checkThreshold(PriceAlert alert, BigDecimal currentPrice) {
        AlertType type = alert.getType();
        BigDecimal threshold = alert.getThresholdValue();

        switch (type) {
            case PRICE_ABOVE:
                return currentPrice.compareTo(threshold) >= 0;

            case PRICE_BELOW:
                return currentPrice.compareTo(threshold) <= 0;

            case PERCENTAGE_UP:
                BigDecimal increase = calculatePercentageChange(alert.getInitialPrice(), currentPrice);
                return increase.compareTo(threshold) >= 0;

            case PERCENTAGE_DOWN:
                BigDecimal change = calculatePercentageChange(alert.getInitialPrice(), currentPrice);
                return change.compareTo(threshold) <= 0;

            default:
                return false;
        }
    }

    private BigDecimal calculatePercentageChange(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice == null || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return newPrice.subtract(oldPrice)
                .divide(oldPrice, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Transactional
    public void triggerAlert(PriceAlert alert, BigDecimal currentPrice) {
        alert.setStatus(AlertStatus.TRIGGERED);
        alert.setTriggeredAt(LocalDateTime.now());
        alert.setTriggeredPrice(currentPrice);
        alert.setNotificationMessage(generateNotificationMessage(alert, currentPrice));

        kafkaProducerService.sendAlertTriggered(alert);

        log.debug("Alert {} triggered for user {}: {} reached {} (current: ${})",
                alert.getId(), alert.getUser().getId(),
                alert.getCoin().getName(), alert.getThresholdValue(),
                currentPrice.setScale(2, RoundingMode.HALF_UP));
    }

    private String generateNotificationMessage(PriceAlert alert, BigDecimal currentPrice) {
        String coinName = alert.getCoin().getName();
        String formattedPrice = currentPrice.setScale(2, RoundingMode.HALF_UP).toPlainString();
        String formattedThreshold = alert.getThresholdValue().setScale(2, RoundingMode.HALF_UP).toPlainString();

        switch (alert.getType()) {
            case PRICE_ABOVE:
                return String.format(PRICE_ABOVE_ALERT_MESSAGE,
                        coinName, formattedThreshold, formattedPrice);

            case PRICE_BELOW:
                return String.format(PRICE_BELOW_ALERT_MESSAGE,
                        coinName, formattedThreshold, formattedPrice);

            case PERCENTAGE_UP:
                return String.format(PERCENTAGE_UP_ALERT_MESSAGE,
                        coinName, formattedThreshold, formattedPrice);

            case PERCENTAGE_DOWN:
                return String.format(PERCENTAGE_DOWN_ALERT_MESSAGE,
                        coinName, formattedThreshold.replace("-", ""), formattedPrice);

            default:
                return String.format("%s alert triggered at $%s", coinName, formattedPrice);
        }
    }
}