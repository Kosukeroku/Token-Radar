package kosukeroku.token_radar.service;

import kosukeroku.token_radar.dto.TrackedCurrencyRequestDto;
import kosukeroku.token_radar.dto.TrackedCurrencyResponseDto;
import kosukeroku.token_radar.exception.CoinNotFoundException;
import kosukeroku.token_radar.exception.TrackedCurrencyAlreadyExistsException;
import kosukeroku.token_radar.exception.TrackedCurrencyNotFoundException;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.model.TrackedCurrency;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.repository.CoinRepository;
import kosukeroku.token_radar.repository.TrackedCurrencyRepository;
import kosukeroku.token_radar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackedCurrencyServiceTest {

    @Mock
    private TrackedCurrencyRepository trackedCurrencyRepository;

    @Mock
    private CoinRepository coinRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TrackedCurrencyService trackedCurrencyService;

    private User testUser;
    private Coin testCoin;
    private TrackedCurrency testTrackedCurrency;
    private TrackedCurrencyRequestDto requestDto;

    @BeforeEach
    void setUp() {
        // setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // setup test coin
        testCoin = new Coin();
        testCoin.setId("bitcoin");
        testCoin.setName("Bitcoin");
        testCoin.setSymbol("BTC");
        testCoin.setCurrentPrice(BigDecimal.valueOf(50000));
        testCoin.setMarketCapRank(1);
        testCoin.setImageUrl("http://example.com/bitcoin.png");

        // setup tracked currency
        testTrackedCurrency = new TrackedCurrency();
        testTrackedCurrency.setId(1L);
        testTrackedCurrency.setUser(testUser);
        testTrackedCurrency.setCoin(testCoin);
        testTrackedCurrency.setAddedAt(LocalDateTime.now());

        // setup request DTO
        requestDto = new TrackedCurrencyRequestDto();
        requestDto.setCoinId("bitcoin");
    }

    @Test
    void addTrackedCurrency_Success() {
        // given
        when(coinRepository.findById("bitcoin")).thenReturn(Optional.of(testCoin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trackedCurrencyRepository.existsByUserIdAndCoinId(1L, "bitcoin")).thenReturn(false);
        when(trackedCurrencyRepository.save(any(TrackedCurrency.class))).thenReturn(testTrackedCurrency);

        // when
        TrackedCurrencyResponseDto result = trackedCurrencyService.addTrackedCurrency(1L, requestDto);

        // then
        assertThat(result)
                .isNotNull()
                .satisfies(dto -> {
                    assertThat(dto.getCoinId()).isEqualTo("bitcoin");
                    assertThat(dto.getCoinName()).isEqualTo("Bitcoin");
                });

        verify(trackedCurrencyRepository).save(any(TrackedCurrency.class));
    }

    @Test
    void addTrackedCurrency_CoinNotFound() {
        // given
        when(coinRepository.findById("bitcoin")).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> trackedCurrencyService.addTrackedCurrency(1L, requestDto))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Coin not found");

        verify(trackedCurrencyRepository, never()).save(any());
    }

    @Test
    void addTrackedCurrency_UserNotFound() {
        // given
        when(coinRepository.findById("bitcoin")).thenReturn(Optional.of(testCoin));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> trackedCurrencyService.addTrackedCurrency(1L, requestDto))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(trackedCurrencyRepository, never()).save(any());
    }

    @Test
    void addTrackedCurrency_AlreadyTracked() {
        // given
        when(coinRepository.findById("bitcoin")).thenReturn(Optional.of(testCoin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trackedCurrencyRepository.existsByUserIdAndCoinId(1L, "bitcoin")).thenReturn(true);

        // then
        assertThatThrownBy(() -> trackedCurrencyService.addTrackedCurrency(1L, requestDto))
                .isInstanceOf(TrackedCurrencyAlreadyExistsException.class)
                .hasMessageContaining("already tracked");

        verify(trackedCurrencyRepository, never()).save(any());
    }

    @Test
    void removeTrackedCurrency_Success() {
        // given
        when(trackedCurrencyRepository.findByUserIdAndCoinId(1L, "bitcoin"))
                .thenReturn(Optional.of(testTrackedCurrency));

        // when
        trackedCurrencyService.removeTrackedCurrency(1L, "bitcoin");

        // then
        verify(trackedCurrencyRepository).delete(testTrackedCurrency);
    }

    @Test
    void removeTrackedCurrency_NotFound() {
        // given
        when(trackedCurrencyRepository.findByUserIdAndCoinId(1L, "bitcoin"))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> trackedCurrencyService.removeTrackedCurrency(1L, "bitcoin"))
                .isInstanceOf(TrackedCurrencyNotFoundException.class)
                .hasMessageContaining("Tracked currency bitcoin not found for user: 1");

        verify(trackedCurrencyRepository, never()).delete(any());
    }

    @Test
    void getUserTrackedCurrencies_Success() {
        // given
        List<TrackedCurrency> trackedCurrencies = List.of(testTrackedCurrency);
        when(trackedCurrencyRepository.findByUserIdOrderByCoinMarketCapRankAsc(1L))
                .thenReturn(trackedCurrencies);

        // when
        List<TrackedCurrencyResponseDto> result = trackedCurrencyService.getUserTrackedCurrencies(1L);

        // then
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .first()
                .satisfies(dto -> {
                    assertThat(dto.getCoinId()).isEqualTo("bitcoin");
                    assertThat(dto.getCoinName()).isEqualTo("Bitcoin");
                });
    }

    @Test
    void getUserTrackedCurrencies_EmptyList() {
        // given
        when(trackedCurrencyRepository.findByUserIdOrderByCoinMarketCapRankAsc(1L))
                .thenReturn(List.of());

        // when
        List<TrackedCurrencyResponseDto> result = trackedCurrencyService.getUserTrackedCurrencies(1L);

        // then
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getUserTrackedCount_Success() {
        // given
        when(trackedCurrencyRepository.countByUserId(1L)).thenReturn(5L);

        // when
        Long count = trackedCurrencyService.getUserTrackedCount(1L);

        // then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    void getUserTrackedCount_Zero() {
        // given
        when(trackedCurrencyRepository.countByUserId(1L)).thenReturn(0L);

        // when
        Long count = trackedCurrencyService.getUserTrackedCount(1L);

        // then
        assertThat(count).isZero();
    }
}