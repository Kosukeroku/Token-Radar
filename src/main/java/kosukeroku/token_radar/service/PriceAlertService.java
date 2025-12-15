package kosukeroku.token_radar.service;

import jakarta.validation.ValidationException;
import kosukeroku.token_radar.dto.*;
import kosukeroku.token_radar.exception.CoinNotFoundException;
import kosukeroku.token_radar.exception.AlertNotFoundException;
import kosukeroku.token_radar.exception.AlertValidationException;
import kosukeroku.token_radar.mapper.PriceAlertMapper;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.model.PriceAlert;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.model.enums.AlertStatus;
import kosukeroku.token_radar.model.enums.AlertType;
import kosukeroku.token_radar.repository.CoinRepository;
import kosukeroku.token_radar.repository.PriceAlertRepository;
import kosukeroku.token_radar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final CoinRepository coinRepository;
    private final UserRepository userRepository;
    private final PriceAlertMapper priceAlertMapper;

    @Transactional
    public PriceAlertResponseDto createAlert(Long userId, PriceAlertRequestDto request) {
        log.info("Creating/updating alert for user {}: {}", userId, request);

        validateAlertRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        Coin coin = coinRepository.findById(request.getCoinId())
                .orElseThrow(() -> new CoinNotFoundException(request.getCoinId()));

        // searching for an existing alert of the same type
        Optional<PriceAlert> existingAlert = priceAlertRepository.findByUserIdAndCoinIdAndTypeAndStatus(
                userId, request.getCoinId(), request.getType(), AlertStatus.ACTIVE);

        PriceAlert alert;

        if (existingAlert.isPresent()) {
            // updating existing alert
            log.info("Updating existing alert {} for user {}", existingAlert.get().getId(), userId);
            alert = existingAlert.get();
            alert.setThresholdValue(request.getThresholdValue());
            alert.setInitialPrice(coin.getCurrentPrice());
            alert.setUpdatedAt(LocalDateTime.now());
        } else {
            // creating new alert
            log.info("Creating new alert for user {}", userId);
            alert = new PriceAlert(user, coin, request.getType(), request.getThresholdValue());
        }

        PriceAlert savedAlert = priceAlertRepository.save(alert);
        log.info("Saved price alert {} for user {}: {} {}",
                savedAlert.getId(), userId, coin.getName(), request.getType());

        return priceAlertMapper.toDto(savedAlert);
    }

    private void validateAlertRequest(PriceAlertRequestDto request) {
        BigDecimal threshold = request.getThresholdValue();

        switch (request.getType()) {
            case PRICE_ABOVE:
                BigDecimal currentPrice = coinRepository.findById(request.getCoinId())
                        .orElseThrow(() -> new CoinNotFoundException("Coin not found: " + request.getCoinId()))
                        .getCurrentPrice();

                if (threshold.compareTo(currentPrice) <= 0) {
                    throw new AlertValidationException("Must be above current!");
                }

                if (threshold.compareTo(BigDecimal.valueOf(1_000_000_000)) > 0) {
                    throw new AlertValidationException("Cannot exceed $1,000,000,000!");
                }
                break;

            case PRICE_BELOW:
                currentPrice = coinRepository.findById(request.getCoinId())
                        .orElseThrow(() -> new CoinNotFoundException("Coin not found: " + request.getCoinId()))
                        .getCurrentPrice();

                if (threshold.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new AlertValidationException("Must be > 0!");
                }

                if (threshold.compareTo(currentPrice) >= 0) {
                    throw new AlertValidationException("Must be below current!");
                }
                break;

            case PERCENTAGE_UP:
                if (threshold.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new AlertValidationException("Must be positive!");
                }
                if (threshold.compareTo(BigDecimal.valueOf(1000)) > 0) {
                    throw new AlertValidationException("Cannot exceed 1000%!");
                }
                break;

            case PERCENTAGE_DOWN:
                if (threshold.compareTo(BigDecimal.ZERO) >= 0) {
                    throw new AlertValidationException("Must be negative!");
                }
                if (threshold.compareTo(BigDecimal.valueOf(-100)) < 0) {
                    throw new AlertValidationException("Cannot exceed -100%!");
                }
                break;
        }
    }

    @Transactional(readOnly = true)
    public List<PriceAlertResponseDto> getUserAlerts(Long userId) {
        return priceAlertRepository.findByUserIdAndStatus(userId, AlertStatus.ACTIVE).stream()
                .map(priceAlertMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PriceAlertResponseDto> getUserAlertsByStatus(Long userId, AlertStatus status) {
        return priceAlertRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status).stream()
                .map(priceAlertMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAlert(Long userId, Long alertId) {
        PriceAlert alert = priceAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));

        if (alert.getStatus() == AlertStatus.ACTIVE || alert.getStatus() == AlertStatus.READ) {
            priceAlertRepository.delete(alert);
            log.info("Deleted {} alert {} for user {}",
                    alert.getStatus(), alertId, userId);
        } else if (alert.getStatus() == AlertStatus.TRIGGERED) {
            // cannot delete triggered alerts, only read or active
            throw new IllegalStateException("Cannot delete TRIGGERED alert. Mark as READ first.");
        }
    }

    @Transactional
    public PriceAlertResponseDto markAsRead(Long userId, Long alertId) {
        PriceAlert alert = priceAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));

        if (alert.getStatus() == AlertStatus.TRIGGERED) {
            alert.setStatus(AlertStatus.READ);
            PriceAlert updated = priceAlertRepository.save(alert);
            log.debug("Alert {} marked as read", alertId);
            return priceAlertMapper.toDto(updated);
        }

        return priceAlertMapper.toDto(alert);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        int updated = priceAlertRepository.markAllTriggeredAsRead(userId);
        log.info("Marked {} alerts as read for user {}", updated, userId);
    }

    @Transactional
    public void clearReadAlerts(Long userId) {
        int deleted = priceAlertRepository.deleteReadAlerts(userId);
        log.info("Deleted {} read alerts for user {}", deleted, userId);
    }

    @Transactional(readOnly = true)
    public AlertStatsDto getAlertStats(Long userId) {
        List<PriceAlert> allAlerts = priceAlertRepository.findByUserId(userId);

        long total = allAlerts.size();
        long active = allAlerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.ACTIVE)
                .count();
        long triggered = allAlerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.TRIGGERED)
                .count();
        long read = allAlerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.READ)
                .count();

        return new AlertStatsDto(total, active, triggered, read, triggered);
    }

    @Transactional(readOnly = true)
    public List<PriceAlertResponseDto> getUserAlertsForCoin(Long userId, String coinId) {
        List<PriceAlert> alerts = priceAlertRepository.findByUserIdAndCoinId(userId, coinId);
        return alerts.stream()
                .map(priceAlertMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PriceAlertResponseDto> getUserNotifications(Long userId) {
        log.info("Getting notifications for user {}", userId);

        List<PriceAlert> triggered = priceAlertRepository.findByUserIdAndStatus(
                userId, AlertStatus.TRIGGERED);
        List<PriceAlert> read = priceAlertRepository.findByUserIdAndStatus(
                userId, AlertStatus.READ);

        List<PriceAlert> allNotifications = new ArrayList<>();
        allNotifications.addAll(triggered);
        allNotifications.addAll(read);

        log.info("Found {} notifications for user {} ({} triggered, {} read)",
                allNotifications.size(), userId, triggered.size(), read.size());

        return allNotifications.stream()
                .map(priceAlertMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return priceAlertRepository.countTriggeredAlerts(userId);
    }
}