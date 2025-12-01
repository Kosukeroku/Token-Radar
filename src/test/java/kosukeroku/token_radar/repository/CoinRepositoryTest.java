package kosukeroku.token_radar.repository;

import kosukeroku.token_radar.model.Coin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CoinRepositoryTest {

    @Autowired
    private CoinRepository coinRepository;

    private Coin bitcoin;
    private Coin ethereum;
    private Coin inactiveCoin;

    @BeforeEach
    void setUp() {
        bitcoin = createCoin("bitcoin", "Bitcoin", "btc", 1, true);
        ethereum = createCoin("ethereum", "Ethereum", "eth", 2, true);
        inactiveCoin = createCoin("inactive", "Inactive Coin", "inc", 100, false);

        coinRepository.saveAll(List.of(bitcoin, ethereum, inactiveCoin));
    }

    @Test
    void findAllActiveCoinIds_ShouldReturnOnlyActiveCoinIds() {
        // when
        List<String> activeCoinIds = coinRepository.findAllActiveCoinIds();

        // then
        assertThat(activeCoinIds)
                .hasSize(2)
                .containsExactly("bitcoin", "ethereum")
                .doesNotContain("inactive");
    }

    @Test
    void findByActiveTrue_ShouldReturnPaginatedActiveCoins() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("marketCapRank").ascending());

        // when
        Page<Coin> result = coinRepository.findByActiveTrue(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Coin::getId)
                .containsExactly("bitcoin", "ethereum");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void findByActiveTrue_ShouldReturnEmpty_WhenNoActiveCoins() {
        // given
        coinRepository.deleteAll();
        Coin anotherInactive = createCoin("inactive2", "Inactive 2", "inc2", 101, false);
        coinRepository.save(anotherInactive);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Coin> result = coinRepository.findByActiveTrue(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void deleteByActiveFalse_ShouldRemoveOnlyInactiveCoins() {
        // given (we have 1 inactive coin out of 3)

        // when
        int deletedCount = coinRepository.deleteByActiveFalse();

        // then
        assertThat(deletedCount).isEqualTo(1);

        List<Coin> remainingCoins = coinRepository.findAll();
        assertThat(remainingCoins)
                .hasSize(2)
                .extracting(Coin::getId)
                .containsExactly("bitcoin", "ethereum");
    }

    @Test
    void deleteByActiveFalse_ShouldDoNothing_WhenAllCoinsActive() {
        // given
        coinRepository.deleteAll();
        coinRepository.saveAll(List.of(bitcoin, ethereum)); // active only

        // when
        int deletedCount = coinRepository.deleteByActiveFalse();

        // then
        assertThat(deletedCount).isEqualTo(0);
        assertThat(coinRepository.count()).isEqualTo(2);
    }

    private Coin createCoin(String id, String name, String symbol, int rank, boolean active) {
        Coin coin = new Coin();
        coin.setId(id);
        coin.setName(name);
        coin.setSymbol(symbol);
        coin.setMarketCapRank(rank);
        coin.setCurrentPrice(new BigDecimal("1000"));
        coin.setMarketCap(new BigDecimal("1000000"));
        coin.setTotalVolume(new BigDecimal("100000"));
        coin.setActive(active);
        return coin;
    }
}