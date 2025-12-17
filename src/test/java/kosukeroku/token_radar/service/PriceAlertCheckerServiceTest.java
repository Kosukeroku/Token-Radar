package kosukeroku.token_radar.service;

import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.model.PriceAlert;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.model.enums.AlertStatus;
import kosukeroku.token_radar.model.enums.AlertType;
import kosukeroku.token_radar.repository.PriceAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAlertCheckerServiceTest {

    @Mock
    private PriceAlertRepository priceAlertRepository;

    @InjectMocks
    private PriceAlertCheckerService priceAlertCheckerService;

    @Captor
    private ArgumentCaptor<List<PriceAlert>> alertsCaptor;

    private User testUser;
    private Coin testCoin;
    private PriceAlert priceAboveAlert;
    private PriceAlert priceBelowAlert;
    private PriceAlert percentageUpAlert;
    private PriceAlert percentageDownAlert;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testCoin = new Coin();
        testCoin.setId("bitcoin");
        testCoin.setName("Bitcoin");
        testCoin.setCurrentPrice(BigDecimal.valueOf(50000));

        // price above alert (target: 60000, current: 50000)
        priceAboveAlert = createAlert(AlertType.PRICE_ABOVE, BigDecimal.valueOf(60000), BigDecimal.valueOf(50000));

        // price below alert (target: 40000, current: 50000)
        priceBelowAlert = createAlert(AlertType.PRICE_BELOW, BigDecimal.valueOf(40000), BigDecimal.valueOf(50000));

        // percentage Up alert (target: +10%, initial: 50000)
        percentageUpAlert = createAlert(AlertType.PERCENTAGE_UP, BigDecimal.valueOf(10), BigDecimal.valueOf(50000));

        // percentage down alert (target: -10%, initial: 50000)
        percentageDownAlert = createAlert(AlertType.PERCENTAGE_DOWN, BigDecimal.valueOf(-10), BigDecimal.valueOf(50000));
    }

    @Test
    void checkAndTriggerAlerts_PriceAbove_ShouldTrigger() {
        // given: price increases to 61000 (above threshold 60000)
        List<PriceAlert> activeAlerts = List.of(priceAboveAlert);
        when(priceAlertRepository.findActiveAlertsForCoin("bitcoin"))
                .thenReturn(activeAlerts);

        // when
        List<PriceAlert> triggered = priceAlertCheckerService.checkAndTriggerAlerts(
                "bitcoin", BigDecimal.valueOf(61000));

        // then
        assertThat(triggered).hasSize(1);
        assertThat(triggered.getFirst().getStatus()).isEqualTo(AlertStatus.TRIGGERED);
        verify(priceAlertRepository).saveAll(alertsCaptor.capture());

        List<PriceAlert> savedAlerts = alertsCaptor.getValue();
        assertThat(savedAlerts.getFirst().getTriggeredPrice())
                .isEqualByComparingTo(BigDecimal.valueOf(61000));
    }

    @Test
    void checkAndTriggerAlerts_PriceAbove_ShouldNotTrigger() {
        // given: price is 59000 (below threshold 60000)
        List<PriceAlert> activeAlerts = List.of(priceAboveAlert);
        when(priceAlertRepository.findActiveAlertsForCoin("bitcoin"))
                .thenReturn(activeAlerts);

        // when
        List<PriceAlert> triggered = priceAlertCheckerService.checkAndTriggerAlerts(
                "bitcoin", BigDecimal.valueOf(59000));

        // then
        assertThat(triggered).isEmpty();
        verify(priceAlertRepository, never()).saveAll(any());
    }

    @Test
    void checkAndTriggerAlerts_PriceBelow_ShouldTrigger() {
        // given: price drops to 39000 (below threshold 40000)
        List<PriceAlert> activeAlerts = List.of(priceBelowAlert);
        when(priceAlertRepository.findActiveAlertsForCoin("bitcoin"))
                .thenReturn(activeAlerts);

        // when
        List<PriceAlert> triggered = priceAlertCheckerService.checkAndTriggerAlerts(
                "bitcoin", BigDecimal.valueOf(39000));

        // then
        assertThat(triggered).hasSize(1);
        assertThat(triggered.get(0).getStatus()).isEqualTo(AlertStatus.TRIGGERED);
    }

    @Test
    void checkAndTriggerAlerts_PercentageUp_ShouldTrigger() {
        // given: price increases to 56000 (+12% from 50000, target +10%)
        List<PriceAlert> activeAlerts = List.of(percentageUpAlert);
        when(priceAlertRepository.findActiveAlertsForCoin("bitcoin"))
                .thenReturn(activeAlerts);

        // when
        List<PriceAlert> triggered = priceAlertCheckerService.checkAndTriggerAlerts(
                "bitcoin", BigDecimal.valueOf(56000));

        // then
        assertThat(triggered).hasSize(1);
        assertThat(triggered.getFirst().getStatus()).isEqualTo(AlertStatus.TRIGGERED);
    }

    @Test
    void checkAndTriggerAlerts_PercentageDown_ShouldTrigger() {
        // given: price drops to 44000 (-12% from 50000, target -10%)
        List<PriceAlert> activeAlerts = List.of(percentageDownAlert);
        when(priceAlertRepository.findActiveAlertsForCoin("bitcoin"))
                .thenReturn(activeAlerts);

        // when
        List<PriceAlert> triggered = priceAlertCheckerService.checkAndTriggerAlerts(
                "bitcoin", BigDecimal.valueOf(44000));

        // then
        assertThat(triggered).hasSize(1);
        assertThat(triggered.getFirst().getStatus()).isEqualTo(AlertStatus.TRIGGERED);
    }

    @Test
    void checkAndTriggerAlerts_ShouldNotTriggerNonActiveAlerts() {
        // given: alert is already triggered
        priceAboveAlert.setStatus(AlertStatus.TRIGGERED);
        List<PriceAlert> activeAlerts = List.of(priceAboveAlert);
        when(priceAlertRepository.findActiveAlertsForCoin("bitcoin"))
                .thenReturn(activeAlerts);

        // when
        List<PriceAlert> triggered = priceAlertCheckerService.checkAndTriggerAlerts(
                "bitcoin", BigDecimal.valueOf(61000));

        // then
        assertThat(triggered).isEmpty();
    }

    @Test
    void checkAndTriggerAlerts_MultipleAlerts_MixedResults() {
        // given: mix of alerts that should and shouldn't trigger
        priceAboveAlert.setThresholdValue(BigDecimal.valueOf(55000)); // should trigger at 56000
        priceBelowAlert.setThresholdValue(BigDecimal.valueOf(54000)); // should not trigger at 56000

        List<PriceAlert> activeAlerts = List.of(priceAboveAlert, priceBelowAlert);
        when(priceAlertRepository.findActiveAlertsForCoin("bitcoin"))
                .thenReturn(activeAlerts);

        // when
        List<PriceAlert> triggered = priceAlertCheckerService.checkAndTriggerAlerts(
                "bitcoin", BigDecimal.valueOf(56000));

        // then
        assertThat(triggered).hasSize(1);
        assertThat(triggered.getFirst().getType()).isEqualTo(AlertType.PRICE_ABOVE);
    }

    @Test
    void checkAndTriggerAlerts_EmptyList_WhenNoActiveAlerts() {
        // given: no active alerts
        when(priceAlertRepository.findActiveAlertsForCoin("bitcoin"))
                .thenReturn(List.of());

        // when
        List<PriceAlert> triggered = priceAlertCheckerService.checkAndTriggerAlerts(
                "bitcoin", BigDecimal.valueOf(56000));

        // then
        assertThat(triggered).isEmpty();
        verify(priceAlertRepository, never()).saveAll(any());
    }

    @Test
    void checkAndTriggerAlerts_NotificationMessage_PriceAbove() {
        // given
        List<PriceAlert> activeAlerts = List.of(priceAboveAlert);
        when(priceAlertRepository.findActiveAlertsForCoin("bitcoin"))
                .thenReturn(activeAlerts);

        // when
        List<PriceAlert> triggered = priceAlertCheckerService.checkAndTriggerAlerts(
                "bitcoin", BigDecimal.valueOf(61000));

        // then
        assertThat(triggered).hasSize(1);
        String expectedMessage = "Bitcoin reached your price target (60000.00)! Price: $61000.00";
        assertThat(triggered.getFirst().getNotificationMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void checkAndTriggerAlerts_NotificationMessage_PercentageUp() {
        // given
        List<PriceAlert> activeAlerts = List.of(percentageUpAlert);
        when(priceAlertRepository.findActiveAlertsForCoin("bitcoin"))
                .thenReturn(activeAlerts);

        // when
        List<PriceAlert> triggered = priceAlertCheckerService.checkAndTriggerAlerts(
                "bitcoin", BigDecimal.valueOf(56000));

        // then
        assertThat(triggered).hasSize(1);
        String expectedMessage = "Bitcoin reached your % target (10.00%)! Current price: $56000.00";
        assertThat(triggered.getFirst().getNotificationMessage()).isEqualTo(expectedMessage);
    }

    private PriceAlert createAlert(AlertType type, BigDecimal threshold, BigDecimal initialPrice) {
        PriceAlert alert = new PriceAlert();
        alert.setId(1L);
        alert.setUser(testUser);
        alert.setCoin(testCoin);
        alert.setType(type);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setThresholdValue(threshold);
        alert.setInitialPrice(initialPrice);
        alert.setLastCheckedPrice(initialPrice);
        return alert;
    }
}