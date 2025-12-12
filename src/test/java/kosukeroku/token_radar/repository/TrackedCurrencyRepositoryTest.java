package kosukeroku.token_radar.repository;

import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.model.TrackedCurrency;
import kosukeroku.token_radar.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrackedCurrencyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TrackedCurrencyRepository trackedCurrencyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoinRepository coinRepository;

    private User testUser;
    private Coin testCoin;

    @BeforeEach
    void setUp() {
        // creating test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@mail.com");
        testUser.setPassword("password");
        entityManager.persist(testUser);

        // creating test coin
        testCoin = new Coin();
        testCoin.setId("bitcoin");
        testCoin.setName("Bitcoin");
        testCoin.setSymbol("BTC");
        testCoin.setCurrentPrice(BigDecimal.valueOf(50000));
        testCoin.setMarketCapRank(1);
        testCoin.setActive(true);
        entityManager.persist(testCoin);

        entityManager.flush();
    }

    @Test
    void findByUserId_Success() {
        // given
        TrackedCurrency trackedCurrency = new TrackedCurrency();
        trackedCurrency.setUser(testUser);
        trackedCurrency.setCoin(testCoin);
        entityManager.persist(trackedCurrency);
        entityManager.flush();

        // when
        List<TrackedCurrency> result = trackedCurrencyRepository.findByUserId(testUser.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoin().getId()).isEqualTo("bitcoin");
    }

    @Test
    void findByUserId_Empty() {
        // when
        List<TrackedCurrency> result = trackedCurrencyRepository.findByUserId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_Paginated() {
        // given
        TrackedCurrency trackedCurrency = new TrackedCurrency();
        trackedCurrency.setUser(testUser);
        trackedCurrency.setCoin(testCoin);
        entityManager.persist(trackedCurrency);
        entityManager.flush();

        // when
        Page<TrackedCurrency> page = trackedCurrencyRepository.findByUserId(testUser.getId(), PageRequest.of(0, 10));

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void existsByUserIdAndCoinId_True() {
        // given
        TrackedCurrency trackedCurrency = new TrackedCurrency();
        trackedCurrency.setUser(testUser);
        trackedCurrency.setCoin(testCoin);
        entityManager.persist(trackedCurrency);
        entityManager.flush();

        // when
        boolean exists = trackedCurrencyRepository.existsByUserIdAndCoinId(
                testUser.getId(), "bitcoin"
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndCoinId_False() {
        // when
        boolean exists = trackedCurrencyRepository.existsByUserIdAndCoinId(testUser.getId(), "ethereum");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void findByUserIdAndCoinId_Success() {
        // given
        TrackedCurrency trackedCurrency = new TrackedCurrency();
        trackedCurrency.setUser(testUser);
        trackedCurrency.setCoin(testCoin);
        entityManager.persist(trackedCurrency);
        entityManager.flush();

        // when
        Optional<TrackedCurrency> result = trackedCurrencyRepository.findByUserIdAndCoinId(testUser.getId(), "bitcoin");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCoin().getId()).isEqualTo("bitcoin");
    }

    @Test
    void deleteByUserIdAndCoinId_Success() {
        // given
        TrackedCurrency trackedCurrency = new TrackedCurrency();
        trackedCurrency.setUser(testUser);
        trackedCurrency.setCoin(testCoin);
        entityManager.persist(trackedCurrency);
        entityManager.flush();

        // when
        trackedCurrencyRepository.deleteByUserIdAndCoinId(testUser.getId(), "bitcoin");

        // then
        List<TrackedCurrency> remaining = trackedCurrencyRepository.findAll();
        assertThat(remaining).isEmpty();
    }

    @Test
    void countByUserId_Success() {
        // given
        TrackedCurrency trackedCurrency = new TrackedCurrency();
        trackedCurrency.setUser(testUser);
        trackedCurrency.setCoin(testCoin);
        entityManager.persist(trackedCurrency);

        // adding another coin
        Coin ethereum = new Coin();
        ethereum.setId("ethereum");
        ethereum.setName("Ethereum");
        ethereum.setSymbol("ETH");
        entityManager.persist(ethereum);

        TrackedCurrency trackedCurrency2 = new TrackedCurrency();
        trackedCurrency2.setUser(testUser);
        trackedCurrency2.setCoin(ethereum);
        entityManager.persist(trackedCurrency2);

        entityManager.flush();

        // when
        Long count = trackedCurrencyRepository.countByUserId(testUser.getId());

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void findByUserIdOrderByCoinMarketCapRankAsc_Success() {
        // given
        Coin ethereum = new Coin();
        ethereum.setId("ethereum");
        ethereum.setName("Ethereum");
        ethereum.setSymbol("ETH");
        ethereum.setMarketCapRank(2);
        ethereum.setCurrentPrice(BigDecimal.valueOf(3000));
        ethereum.setActive(true);
        entityManager.persist(ethereum);

        Coin cardano = new Coin();
        cardano.setId("cardano");
        cardano.setName("Cardano");
        cardano.setSymbol("ADA");
        cardano.setMarketCapRank(3);
        cardano.setCurrentPrice(BigDecimal.valueOf(1.5));
        cardano.setActive(true);
        entityManager.persist(cardano);

        TrackedCurrency trackedBitcoin = new TrackedCurrency();
        trackedBitcoin.setUser(testUser);
        trackedBitcoin.setCoin(testCoin);
        trackedBitcoin.setAddedAt(LocalDateTime.now());
        entityManager.persist(trackedBitcoin);

        TrackedCurrency trackedEthereum = new TrackedCurrency();
        trackedEthereum.setUser(testUser);
        trackedEthereum.setCoin(ethereum);
        trackedEthereum.setAddedAt(LocalDateTime.now());
        entityManager.persist(trackedEthereum);

        TrackedCurrency trackedCardano = new TrackedCurrency();
        trackedCardano.setUser(testUser);
        trackedCardano.setCoin(cardano);
        trackedCardano.setAddedAt(LocalDateTime.now());
        entityManager.persist(trackedCardano);

        entityManager.flush();

        // when
        List<TrackedCurrency> result = trackedCurrencyRepository
                .findByUserIdOrderByCoinMarketCapRankAsc(testUser.getId());

        // then
        assertThat(result)
                .hasSize(3)
                .extracting(tc -> tc.getCoin().getId())
                .containsExactly("bitcoin", "ethereum", "cardano");
    }

    @Test
    void findByUserIdAndCoinId_NotFound() {
        // when
        Optional<TrackedCurrency> result = trackedCurrencyRepository
                .findByUserIdAndCoinId(999L, "nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void deleteByUserIdAndCoinId_WhenNotExists_ShouldDoNothing() {
        // when
        trackedCurrencyRepository.deleteByUserIdAndCoinId(999L, "nonexistent");

        // then
        assertThat(trackedCurrencyRepository.count()).isZero();
    }

}