package kosukeroku.token_radar.service;

import kosukeroku.token_radar.exception.CoinNotFoundException;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.repository.CoinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoinServiceTest {

    @Mock
    private CoinRepository coinRepository;

    @InjectMocks
    private CoinService coinService;

    private Coin bitcoin;
    private Coin ethereum;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        bitcoin = new Coin();
        bitcoin.setId("bitcoin");
        bitcoin.setName("Bitcoin");
        bitcoin.setSymbol("btc");
        bitcoin.setMarketCapRank(1);
        bitcoin.setCurrentPrice(new BigDecimal("100000"));
        bitcoin.setActive(true);

        ethereum = new Coin();
        ethereum.setId("ethereum");
        ethereum.setName("Ethereum");
        ethereum.setSymbol("eth");
        ethereum.setMarketCapRank(2);
        ethereum.setCurrentPrice(new BigDecimal("3000"));
        ethereum.setActive(true);

        pageable = PageRequest.of(0, 20, Sort.by("marketCapRank").ascending());
    }

    @Test
    void getDashboard_ShouldReturnPageOfCoins_WhenCoinsExist() {
        // given
        List<Coin> coins = List.of(bitcoin, ethereum);
        Page<Coin> coinPage = new PageImpl<>(coins, pageable, coins.size());

        when(coinRepository.findByActiveTrue(pageable)).thenReturn(coinPage);

        // when
        Page<Coin> result = coinService.getDashboard(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(bitcoin, ethereum);
        verify(coinRepository).findByActiveTrue(pageable);
    }

    @Test
    void getAllCoins_ShouldReturnAllCoins_WhenCoinsExist() {
        // given
        List<Coin> coins = List.of(bitcoin, ethereum);
        when(coinRepository.findAll()).thenReturn(coins);

        // when
        List<Coin> result = coinService.getAllCoins();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(bitcoin, ethereum);
        verify(coinRepository).findAll();
    }

    @Test
    void searchCoins_ShouldReturnCoins_WhenNameMatches() {
        // given
        List<Coin> coins = List.of(bitcoin, ethereum);
        when(coinRepository.findAll()).thenReturn(coins);

        // when
        List<Coin> result = coinService.searchCoins("bitcoin");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(bitcoin);
        verify(coinRepository).findAll();
    }

    @Test
    void searchCoins_ShouldReturnCoins_WhenNamePartiallyMatches() {
        // given
        List<Coin> coins = List.of(bitcoin, ethereum);
        when(coinRepository.findAll()).thenReturn(coins);

        // when
        List<Coin> result = coinService.searchCoins("bit");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(bitcoin);
        verify(coinRepository).findAll();
    }

    @Test
    void searchCoins_ShouldReturnCoins_WhenSymbolMatches() {
        // given
        List<Coin> coins = List.of(bitcoin, ethereum);
        when(coinRepository.findAll()).thenReturn(coins);

        // when
        List<Coin> result = coinService.searchCoins("eth");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(ethereum);
        verify(coinRepository).findAll();
    }

    @Test
    void searchCoins_ShouldReturnEmptyList_WhenNoMatches() {
        // given
        List<Coin> coins = List.of(bitcoin, ethereum);
        when(coinRepository.findAll()).thenReturn(coins);

        // when
        List<Coin> result = coinService.searchCoins("dogecoin");

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(coinRepository).findAll();
    }

    @Test
    void searchCoins_ShouldBeCaseInsensitive() {
        // given
        List<Coin> coins = List.of(bitcoin, ethereum);
        when(coinRepository.findAll()).thenReturn(coins);

        // when
        List<Coin> result = coinService.searchCoins("BITCOIN");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(bitcoin);
        verify(coinRepository).findAll();
    }

    @Test
    void getCoinById_ShouldReturnCoin_WhenCoinExists() {
        // given
        when(coinRepository.findById("bitcoin")).thenReturn(Optional.of(bitcoin));

        // when
        Coin result = coinService.getCoinById("bitcoin");

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(bitcoin);
        verify(coinRepository).findById("bitcoin");
    }

    @Test
    void getCoinById_ShouldThrowException_WhenCoinDoesNotExist() {
        // given
        when(coinRepository.findById("non-existent")).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> coinService.getCoinById("non-existent"))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessage("Coin not found: non-existent");

        verify(coinRepository).findById("non-existent");
    }

    @Test
    void searchCoins_ShouldReturnMultipleCoins_WhenQueryMatchesMultiple() {
        // given
        Coin binanceCoin = new Coin();
        binanceCoin.setId("binancecoin");
        binanceCoin.setName("Binance Coin");
        binanceCoin.setSymbol("bnb");
        binanceCoin.setMarketCapRank(3);
        binanceCoin.setActive(true);

        List<Coin> coins = List.of(bitcoin, binanceCoin);
        when(coinRepository.findAll()).thenReturn(coins);

        // when
        List<Coin> result = coinService.searchCoins("coin");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(bitcoin, binanceCoin);
        verify(coinRepository).findAll();
    }
}