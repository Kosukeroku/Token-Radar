package kosukeroku.token_radar.repository;

import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.model.PriceAlert;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.model.enums.AlertStatus;
import kosukeroku.token_radar.model.enums.AlertType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PriceAlertRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PriceAlertRepository priceAlertRepository;

    private User testUser;
    private Coin bitcoin;
    private Coin ethereum;

    @BeforeEach
    void setUp() {
        // clean up
        priceAlertRepository.deleteAll();

        // create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@mail.com");
        testUser.setPassword("password");
        entityManager.persist(testUser);

        // create test coins
        bitcoin = new Coin();
        bitcoin.setId("bitcoin");
        bitcoin.setName("Bitcoin");
        bitcoin.setSymbol("BTC");
        bitcoin.setCurrentPrice(BigDecimal.valueOf(50000));
        bitcoin.setActive(true);
        entityManager.persist(bitcoin);

        ethereum = new Coin();
        ethereum.setId("ethereum");
        ethereum.setName("Ethereum");
        ethereum.setSymbol("ETH");
        ethereum.setCurrentPrice(BigDecimal.valueOf(3000));
        ethereum.setActive(true);
        entityManager.persist(ethereum);

        entityManager.flush();
    }

    @Test
    void findByUserIdAndStatus_Success() {
        // given
        PriceAlert alert = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);
        entityManager.persist(alert);
        entityManager.flush();

        // when
        List<PriceAlert> alerts = priceAlertRepository.findByUserIdAndStatus(
                testUser.getId(), AlertStatus.ACTIVE);

        // then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.getFirst().getCoin().getId()).isEqualTo("bitcoin");
        assertThat(alerts.getFirst().getStatus()).isEqualTo(AlertStatus.ACTIVE);
    }

    @Test
    void findByUserIdAndStatusOrderByCreatedAtDesc_Success() {
        // given
        PriceAlert alert1 = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);
        alert1.setCreatedAt(LocalDateTime.now().minusHours(1));

        PriceAlert alert2 = createPriceAlert(testUser, ethereum, AlertType.PRICE_BELOW, AlertStatus.ACTIVE);
        alert2.setCreatedAt(LocalDateTime.now());

        entityManager.persist(alert1);
        entityManager.persist(alert2);
        entityManager.flush();

        // when
        List<PriceAlert> alerts = priceAlertRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                testUser.getId(), AlertStatus.ACTIVE);

        // then
        assertThat(alerts).hasSize(2);

        assertThat(alerts.get(0).getCoin().getId()).isEqualTo("ethereum"); // newer
        assertThat(alerts.get(1).getCoin().getId()).isEqualTo("bitcoin");  // older
    }

    @Test
    void findByCoinIdAndStatus_Success() {
        // given
        PriceAlert alert = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);
        entityManager.persist(alert);
        entityManager.flush();

        // when
        List<PriceAlert> alerts = priceAlertRepository.findByCoinIdAndStatus(
                "bitcoin", AlertStatus.ACTIVE);

        // then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.getFirst().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findActiveAlertsForCoin_Success() {
        // given
        PriceAlert activeAlert = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);
        PriceAlert triggeredAlert = createPriceAlert(testUser, bitcoin, AlertType.PRICE_BELOW, AlertStatus.TRIGGERED);

        entityManager.persist(activeAlert);
        entityManager.persist(triggeredAlert);
        entityManager.flush();

        // when
        List<PriceAlert> alerts = priceAlertRepository.findActiveAlertsForCoin("bitcoin");

        // then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.getFirst().getStatus()).isEqualTo(AlertStatus.ACTIVE);
    }

    @Test
    void findByIdAndUserId_Success() {
        // given
        PriceAlert alert = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);
        entityManager.persist(alert);
        entityManager.flush();

        // when
        Optional<PriceAlert> found = priceAlertRepository.findByIdAndUserId(
                alert.getId(), testUser.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getCoin().getId()).isEqualTo("bitcoin");
    }

    @Test
    void findByIdAndUserId_NotFound() {
        // when
        Optional<PriceAlert> found = priceAlertRepository.findByIdAndUserId(999L, 999L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUserIdAndCoinIdAndTypeAndStatus_Success() {
        // given
        PriceAlert alert = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);
        entityManager.persist(alert);
        entityManager.flush();

        // when
        boolean exists = priceAlertRepository.existsByUserIdAndCoinIdAndTypeAndStatus(
                testUser.getId(), "bitcoin", AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndCoinIdAndTypeAndStatus_False() {
        // when
        boolean exists = priceAlertRepository.existsByUserIdAndCoinIdAndTypeAndStatus(
                testUser.getId(), "ethereum", AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void findByUserId_Success() {
        // given
        PriceAlert alert1 = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);
        PriceAlert alert2 = createPriceAlert(testUser, ethereum, AlertType.PRICE_BELOW, AlertStatus.TRIGGERED);

        entityManager.persist(alert1);
        entityManager.persist(alert2);
        entityManager.flush();

        // when
        List<PriceAlert> alerts = priceAlertRepository.findByUserId(testUser.getId());

        // then
        assertThat(alerts).hasSize(2);
    }

    @Test
    void findByUserIdAndCoinId_Success() {
        // given
        PriceAlert alert1 = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);
        PriceAlert alert2 = createPriceAlert(testUser, bitcoin, AlertType.PRICE_BELOW, AlertStatus.TRIGGERED);

        entityManager.persist(alert1);
        entityManager.persist(alert2);
        entityManager.flush();

        // when
        List<PriceAlert> alerts = priceAlertRepository.findByUserIdAndCoinId(testUser.getId(), "bitcoin");

        // then
        assertThat(alerts).hasSize(2);
    }

    @Test
    void countTriggeredAlerts_Success() {
        // given
        PriceAlert activeAlert = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.ACTIVE);
        PriceAlert triggeredAlert1 = createPriceAlert(testUser, bitcoin, AlertType.PRICE_BELOW, AlertStatus.TRIGGERED);
        PriceAlert triggeredAlert2 = createPriceAlert(testUser, ethereum, AlertType.PERCENTAGE_UP, AlertStatus.TRIGGERED);

        entityManager.persist(activeAlert);
        entityManager.persist(triggeredAlert1);
        entityManager.persist(triggeredAlert2);
        entityManager.flush();

        // when
        long count = priceAlertRepository.countTriggeredAlerts(testUser.getId());

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void markAllTriggeredAsRead_Success() {
        // given
        PriceAlert triggeredAlert1 = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.TRIGGERED);
        PriceAlert triggeredAlert2 = createPriceAlert(testUser, ethereum, AlertType.PRICE_BELOW, AlertStatus.TRIGGERED);
        PriceAlert activeAlert = createPriceAlert(testUser, bitcoin, AlertType.PERCENTAGE_UP, AlertStatus.ACTIVE);

        entityManager.persist(triggeredAlert1);
        entityManager.persist(triggeredAlert2);
        entityManager.persist(activeAlert);
        entityManager.flush();

        // when
        int updated = priceAlertRepository.markAllTriggeredAsRead(testUser.getId());

        // then
        assertThat(updated).isEqualTo(2);

        entityManager.clear();
        PriceAlert updatedAlert1 = entityManager.find(PriceAlert.class, triggeredAlert1.getId());
        PriceAlert updatedAlert2 = entityManager.find(PriceAlert.class, triggeredAlert2.getId());
        PriceAlert unchangedAlert = entityManager.find(PriceAlert.class, activeAlert.getId());

        assertThat(updatedAlert1.getStatus()).isEqualTo(AlertStatus.READ);
        assertThat(updatedAlert2.getStatus()).isEqualTo(AlertStatus.READ);
        assertThat(unchangedAlert.getStatus()).isEqualTo(AlertStatus.ACTIVE);
    }

    @Test
    void deleteReadAlerts_Success() {
        // given
        PriceAlert readAlert1 = createPriceAlert(testUser, bitcoin, AlertType.PRICE_ABOVE, AlertStatus.READ);
        PriceAlert readAlert2 = createPriceAlert(testUser, ethereum, AlertType.PRICE_BELOW, AlertStatus.READ);
        PriceAlert activeAlert = createPriceAlert(testUser, bitcoin, AlertType.PERCENTAGE_UP, AlertStatus.ACTIVE);

        entityManager.persist(readAlert1);
        entityManager.persist(readAlert2);
        entityManager.persist(activeAlert);
        entityManager.flush();

        // when
        int deleted = priceAlertRepository.deleteReadAlerts(testUser.getId());

        // then
        assertThat(deleted).isEqualTo(2);

        List<PriceAlert> remaining = priceAlertRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.getFirst().getStatus()).isEqualTo(AlertStatus.ACTIVE);
    }

    private PriceAlert createPriceAlert(User user, Coin coin, AlertType type, AlertStatus status) {
        PriceAlert alert = new PriceAlert();
        alert.setUser(user);
        alert.setCoin(coin);
        alert.setType(type);
        alert.setStatus(status);
        alert.setThresholdValue(BigDecimal.valueOf(1000));
        alert.setInitialPrice(coin.getCurrentPrice());
        alert.setCreatedAt(LocalDateTime.now());
        return alert;
    }
}