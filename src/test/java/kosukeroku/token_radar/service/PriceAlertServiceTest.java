package kosukeroku.token_radar.service;

import kosukeroku.token_radar.dto.PriceAlertRequestDto;
import kosukeroku.token_radar.dto.PriceAlertResponseDto;
import kosukeroku.token_radar.dto.AlertStatsDto;
import kosukeroku.token_radar.exception.AlertNotFoundException;
import kosukeroku.token_radar.exception.AlertValidationException;
import kosukeroku.token_radar.exception.CoinNotFoundException;
import kosukeroku.token_radar.mapper.PriceAlertMapper;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.model.PriceAlert;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.model.enums.AlertStatus;
import kosukeroku.token_radar.model.enums.AlertType;
import kosukeroku.token_radar.repository.CoinRepository;
import kosukeroku.token_radar.repository.PriceAlertRepository;
import kosukeroku.token_radar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {

    @Mock
    private PriceAlertRepository priceAlertRepository;

    @Mock
    private CoinRepository coinRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PriceAlertMapper priceAlertMapper;

    @InjectMocks
    private PriceAlertService priceAlertService;

    private User testUser;
    private Coin testCoin;
    private PriceAlert testAlert;
    private PriceAlertRequestDto testRequest;
    private PriceAlertResponseDto testResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testCoin = new Coin();
        testCoin.setId("bitcoin");
        testCoin.setName("Bitcoin");
        testCoin.setCurrentPrice(BigDecimal.valueOf(50000));

        testAlert = new PriceAlert();
        testAlert.setId(1L);
        testAlert.setUser(testUser);
        testAlert.setCoin(testCoin);
        testAlert.setType(AlertType.PRICE_ABOVE);
        testAlert.setStatus(AlertStatus.ACTIVE);
        testAlert.setThresholdValue(BigDecimal.valueOf(60000));
        testAlert.setInitialPrice(BigDecimal.valueOf(50000));
        testAlert.setCreatedAt(LocalDateTime.now());

        testRequest = new PriceAlertRequestDto();
        testRequest.setCoinId("bitcoin");
        testRequest.setType(AlertType.PRICE_ABOVE);
        testRequest.setThresholdValue(BigDecimal.valueOf(60000));

        testResponse = new PriceAlertResponseDto();
        testResponse.setId(1L);
        testResponse.setCoinId("bitcoin");
        testResponse.setType(AlertType.PRICE_ABOVE);
        testResponse.setStatus(AlertStatus.ACTIVE);
        testResponse.setThresholdValue(BigDecimal.valueOf(60000));
        testResponse.setIsRead(false);

        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        lenient().when(coinRepository.findById("bitcoin")).thenReturn(Optional.of(testCoin));
    }

    @Test
    void createAlert_Success_NewAlert() {
        // given
        when(priceAlertRepository.findByUserIdAndCoinIdAndTypeAndStatus(
                anyLong(), anyString(), any(AlertType.class), any(AlertStatus.class)))
                .thenReturn(Optional.empty());
        when(priceAlertRepository.save(any(PriceAlert.class))).thenReturn(testAlert);
        when(priceAlertMapper.toDto(testAlert)).thenReturn(testResponse);

        // when
        PriceAlertResponseDto result = priceAlertService.createAlert(1L, testRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCoinId()).isEqualTo("bitcoin");
        verify(priceAlertRepository).save(any(PriceAlert.class));
    }

    @Test
    void createAlert_Success_UpdateExistingAlert() {
        // given
        when(priceAlertRepository.findByUserIdAndCoinIdAndTypeAndStatus(
                anyLong(), anyString(), any(AlertType.class), any(AlertStatus.class)))
                .thenReturn(Optional.of(testAlert));
        when(priceAlertRepository.save(any(PriceAlert.class))).thenReturn(testAlert);
        when(priceAlertMapper.toDto(testAlert)).thenReturn(testResponse);

        // when
        PriceAlertResponseDto result = priceAlertService.createAlert(1L, testRequest);

        // then
        assertThat(result).isNotNull();
        verify(priceAlertRepository).save(testAlert);
    }

    @Test
    void createAlert_UserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> priceAlertService.createAlert(1L, testRequest))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void createAlert_CoinNotFound() {
        // given
        when(coinRepository.findById("bitcoin")).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> priceAlertService.createAlert(1L, testRequest))
                .isInstanceOf(CoinNotFoundException.class);
    }

    @Test
    void createAlert_ValidationFailed_PriceAboveBelowCurrent() {
        // given
        testRequest.setThresholdValue(BigDecimal.valueOf(40000)); // below current price for PRICE_ABOVE

        // then
        assertThatThrownBy(() -> priceAlertService.createAlert(1L, testRequest))
                .isInstanceOf(AlertValidationException.class)
                .hasMessageContaining("Must be above current!");
    }

    @Test
    void createAlert_ValidationFailed_PriceBelowAboveCurrent() {
        // given
        testRequest.setType(AlertType.PRICE_BELOW);
        testRequest.setThresholdValue(BigDecimal.valueOf(55000)); // above current price for PRICE_BELOW

        // then
        assertThatThrownBy(() -> priceAlertService.createAlert(1L, testRequest))
                .isInstanceOf(AlertValidationException.class)
                .hasMessageContaining("Must be below current!");
    }

    @Test
    void createAlert_ValidationFailed_PercentageUpNegative() {
        // given
        testRequest.setType(AlertType.PERCENTAGE_UP);
        testRequest.setThresholdValue(BigDecimal.valueOf(-5)); // negative for PERCENTAGE_UP

        // then
        assertThatThrownBy(() -> priceAlertService.createAlert(1L, testRequest))
                .isInstanceOf(AlertValidationException.class)
                .hasMessageContaining("Must be positive!");
    }

    @Test
    void createAlert_ValidationFailed_PercentageDownPositive() {
        // given
        testRequest.setType(AlertType.PERCENTAGE_DOWN);
        testRequest.setThresholdValue(BigDecimal.valueOf(5)); // positive for PERCENTAGE_DOWN

        // then
        assertThatThrownBy(() -> priceAlertService.createAlert(1L, testRequest))
                .isInstanceOf(AlertValidationException.class)
                .hasMessageContaining("Must be negative!");
    }

    @Test
    void getUserAlerts_Success() {
        // given
        List<PriceAlert> alerts = List.of(testAlert);
        when(priceAlertRepository.findByUserIdAndStatus(1L, AlertStatus.ACTIVE))
                .thenReturn(alerts);
        when(priceAlertMapper.toDto(testAlert)).thenReturn(testResponse);

        // when
        List<PriceAlertResponseDto> result = priceAlertService.getUserAlerts(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCoinId()).isEqualTo("bitcoin");
    }

    @Test
    void getUserAlertsByStatus_Success() {
        // given
        List<PriceAlert> alerts = List.of(testAlert);
        when(priceAlertRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, AlertStatus.ACTIVE))
                .thenReturn(alerts);
        when(priceAlertMapper.toDto(testAlert)).thenReturn(testResponse);

        // when
        List<PriceAlertResponseDto> result = priceAlertService.getUserAlertsByStatus(1L, AlertStatus.ACTIVE);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void deleteAlert_Success() {
        // given
        when(priceAlertRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testAlert));

        // when
        priceAlertService.deleteAlert(1L, 1L);

        // then
        verify(priceAlertRepository).delete(testAlert);
    }

    @Test
    void deleteAlert_NotFound() {
        // given
        when(priceAlertRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> priceAlertService.deleteAlert(1L, 1L))
                .isInstanceOf(AlertNotFoundException.class);
    }

    @Test
    void deleteAlert_CannotDeleteTriggered() {
        // given
        testAlert.setStatus(AlertStatus.TRIGGERED);
        when(priceAlertRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testAlert));

        // then
        assertThatThrownBy(() -> priceAlertService.deleteAlert(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete TRIGGERED alert");
    }

    @Test
    void markAsRead_Success() {
        // given
        testAlert.setStatus(AlertStatus.TRIGGERED);
        when(priceAlertRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testAlert));

        PriceAlert updatedAlert = new PriceAlert();
        updatedAlert.setId(1L);
        updatedAlert.setStatus(AlertStatus.READ);
        updatedAlert.setUser(testUser);
        updatedAlert.setCoin(testCoin);
        updatedAlert.setType(AlertType.PRICE_ABOVE);
        updatedAlert.setThresholdValue(BigDecimal.valueOf(60000));

        when(priceAlertRepository.save(any(PriceAlert.class))).thenReturn(updatedAlert);

        PriceAlertResponseDto expectedResponse = new PriceAlertResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setStatus(AlertStatus.READ);
        expectedResponse.setIsRead(true);

        when(priceAlertMapper.toDto(updatedAlert)).thenReturn(expectedResponse);

        // when
        PriceAlertResponseDto result = priceAlertService.markAsRead(1L, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AlertStatus.READ);
        assertThat(result.getIsRead()).isTrue();
    }

    @Test
    void markAllAsRead_Success() {
        // given
        when(priceAlertRepository.markAllTriggeredAsRead(1L)).thenReturn(3);

        // when
        priceAlertService.markAllAsRead(1L);

        // then
        verify(priceAlertRepository).markAllTriggeredAsRead(1L);
    }

    @Test
    void clearReadAlerts_Success() {
        // given
        when(priceAlertRepository.deleteReadAlerts(1L)).thenReturn(2);

        // when
        priceAlertService.clearReadAlerts(1L);

        // then
        verify(priceAlertRepository).deleteReadAlerts(1L);
    }

    @Test
    void getAlertStats_Success() {
        // given
        List<PriceAlert> allAlerts = List.of(
                createAlertWithStatus(AlertStatus.ACTIVE),
                createAlertWithStatus(AlertStatus.ACTIVE),
                createAlertWithStatus(AlertStatus.TRIGGERED),
                createAlertWithStatus(AlertStatus.TRIGGERED),
                createAlertWithStatus(AlertStatus.TRIGGERED),
                createAlertWithStatus(AlertStatus.READ),
                createAlertWithStatus(AlertStatus.READ)
        );

        when(priceAlertRepository.findByUserId(1L)).thenReturn(allAlerts);

        // when
        AlertStatsDto stats = priceAlertService.getAlertStats(1L);

        // then
        assertThat(stats.getTotalAlerts()).isEqualTo(7L);
        assertThat(stats.getActiveAlerts()).isEqualTo(2L);
        assertThat(stats.getTriggeredAlerts()).isEqualTo(3L);
        assertThat(stats.getReadAlerts()).isEqualTo(2L);
        assertThat(stats.getUnreadCount()).isEqualTo(3L);
    }

    @Test
    void getUserAlertsForCoin_Success() {
        // given
        List<PriceAlert> alerts = List.of(testAlert);
        when(priceAlertRepository.findByUserIdAndCoinId(1L, "bitcoin"))
                .thenReturn(alerts);
        when(priceAlertMapper.toDto(testAlert)).thenReturn(testResponse);

        // when
        List<PriceAlertResponseDto> result = priceAlertService.getUserAlertsForCoin(1L, "bitcoin");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCoinId()).isEqualTo("bitcoin");
    }

    @Test
    void getUserNotifications_Success() {
        // given
        PriceAlert triggeredAlert = createAlertWithStatus(AlertStatus.TRIGGERED);
        PriceAlert readAlert = createAlertWithStatus(AlertStatus.READ);
        readAlert.setId(2L);

        List<PriceAlert> notifications = List.of(triggeredAlert, readAlert);
        when(priceAlertRepository.findNotificationsForUser(eq(1L), anyList()))
                .thenReturn(notifications);

        PriceAlertResponseDto triggeredResponse = new PriceAlertResponseDto();
        triggeredResponse.setId(1L);
        triggeredResponse.setStatus(AlertStatus.TRIGGERED);
        triggeredResponse.setIsRead(false);

        PriceAlertResponseDto readResponse = new PriceAlertResponseDto();
        readResponse.setId(2L);
        readResponse.setStatus(AlertStatus.READ);
        readResponse.setIsRead(true);

        when(priceAlertMapper.toDto(triggeredAlert)).thenReturn(triggeredResponse);
        when(priceAlertMapper.toDto(readAlert)).thenReturn(readResponse);

        // when
        List<PriceAlertResponseDto> result = priceAlertService.getUserNotifications(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(AlertStatus.TRIGGERED);
        assertThat(result.get(1).getStatus()).isEqualTo(AlertStatus.READ);
    }

    @Test
    void getUnreadCount_Success() {
        // given
        when(priceAlertRepository.countTriggeredAlerts(1L)).thenReturn(3L);

        // when
        Long count = priceAlertService.getUnreadCount(1L);

        // then
        assertThat(count).isEqualTo(3L);
    }

    private PriceAlert createAlertWithStatus(AlertStatus status) {
        PriceAlert alert = new PriceAlert();
        alert.setStatus(status);
        return alert;
    }
}