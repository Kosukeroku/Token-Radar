package kosukeroku.token_radar.controller;

import jakarta.persistence.EntityManager;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.model.PriceAlert;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.model.enums.AlertStatus;
import kosukeroku.token_radar.model.enums.AlertType;
import kosukeroku.token_radar.repository.CoinRepository;
import kosukeroku.token_radar.repository.PriceAlertRepository;
import kosukeroku.token_radar.repository.UserRepository;
import kosukeroku.token_radar.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PriceAlertEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PriceAlertRepository priceAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private kosukeroku.token_radar.service.kafka.KafkaConsumerService kafkaConsumerService;

    private User testUser;
    private Coin bitcoin;
    private UserDetailsImpl testUserDetails;

    @BeforeEach
    void setUp() {
        // cleaning up
        priceAlertRepository.deleteAll();
        coinRepository.deleteAll();
        userRepository.deleteAll();

        // creating test user
        testUser = new User();
        testUser.setUsername("integrationuser");
        testUser.setEmail("integration@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // creating test coin
        bitcoin = new Coin();
        bitcoin.setId("bitcoin");
        bitcoin.setName("Bitcoin");
        bitcoin.setSymbol("BTC");
        bitcoin.setCurrentPrice(BigDecimal.valueOf(50000));
        bitcoin.setActive(true);
        bitcoin.setMarketCapRank(1);
        coinRepository.save(bitcoin);

        // creating user details for authentication
        testUserDetails = new UserDetailsImpl(
                testUser.getId(),
                testUser.getUsername(),
                testUser.getEmail(),
                testUser.getPassword(),
                List.of()
        );
    }

    @Test
    void fullAlertLifecycle_Success() throws Exception {
        // 1. creating an alert
        String alertRequest = """
                {
                    "coinId": "bitcoin",
                    "type": "PRICE_ABOVE",
                    "thresholdValue": 60000
                }
                """;

        mockMvc.perform(post("/api/alerts")
                        .with(user(testUserDetails))
                        .contentType("application/json")
                        .content(alertRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinId").value("bitcoin"))
                .andExpect(jsonPath("$.type").value("price_above"))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.thresholdValue").value(60000));

        // verifying alert was created
        List<PriceAlert> alerts = priceAlertRepository.findByUserId(testUser.getId());
        assertThat(alerts).hasSize(1);
        PriceAlert alert = alerts.get(0);
        assertThat(alert.getThresholdValue()).isEqualByComparingTo("60000");
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.ACTIVE);

        // 2. getting user alerts
        mockMvc.perform(get("/api/alerts")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].coinId").value("bitcoin"));

        // 3. getting alert stats
        mockMvc.perform(get("/api/alerts/stats")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAlerts").value(1))
                .andExpect(jsonPath("$.activeAlerts").value(1))
                .andExpect(jsonPath("$.triggeredAlerts").value(0));

        // 4. simulating price change
        bitcoin.setCurrentPrice(BigDecimal.valueOf(61000));
        coinRepository.save(bitcoin);

        alert.setStatus(AlertStatus.TRIGGERED);
        alert.setTriggeredAt(LocalDateTime.now());
        alert.setTriggeredPrice(BigDecimal.valueOf(61000));
        alert.setNotificationMessage("Bitcoin reached your price target (60000)! Price: $61000");
        priceAlertRepository.save(alert);

        // 5. checking notifications
        mockMvc.perform(get("/api/alerts/notifications")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("triggered"))
                .andExpect(jsonPath("$[0].notificationMessage").value("Bitcoin reached your price target (60000)! Price: $61000"));

        // 6. checking unread count
        mockMvc.perform(get("/api/alerts/unread-count")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        // 7. marking as read
        mockMvc.perform(patch("/api/alerts/" + alert.getId() + "/read")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("read"));

        // 8. verifying status changed
        PriceAlert updatedAlert = priceAlertRepository.findById(alert.getId()).orElseThrow();
        assertThat(updatedAlert.getStatus()).isEqualTo(AlertStatus.READ);

        // 9. clearing read alerts
        mockMvc.perform(post("/api/alerts/clear-read")
                        .with(user(testUserDetails)))
                .andExpect(status().isNoContent());

        // 10. synchronizing with database
        entityManager.flush();
        entityManager.clear();

        // 11. verifying alert was deleted
        assertThat(priceAlertRepository.findById(alert.getId())).isEmpty();
    }


@Test
void createMultipleAlertsForSameCoin_DifferentTypes() throws Exception {
    // given
    String priceAboveRequest = """
            {
                "coinId": "bitcoin",
                "type": "PRICE_ABOVE",
                "thresholdValue": 60000
            }
            """;

    // then
    mockMvc.perform(post("/api/alerts")
                    .with(user(testUserDetails))
                    .contentType("application/json")
                    .content(priceAboveRequest))
            .andExpect(status().isOk());

    // given
    String percentageUpRequest = """
            {
                "coinId": "bitcoin",
                "type": "PERCENTAGE_UP",
                "thresholdValue": 10
            }
            """;

    // then
    mockMvc.perform(post("/api/alerts")
                    .with(user(testUserDetails))
                    .contentType("application/json")
                    .content(percentageUpRequest))
            .andExpect(status().isOk());

    List<PriceAlert> alerts = priceAlertRepository.findByUserId(testUser.getId());
    assertThat(alerts).hasSize(2);

    assertThat(alerts)
            .extracting(PriceAlert::getType)
            .containsExactlyInAnyOrder(AlertType.PRICE_ABOVE, AlertType.PERCENTAGE_UP);
}

@Test
void deleteAlert_Success() throws Exception {
    // given
    PriceAlert alert = new PriceAlert();
    alert.setUser(testUser);
    alert.setCoin(bitcoin);
    alert.setType(AlertType.PRICE_ABOVE);
    alert.setStatus(AlertStatus.ACTIVE);
    alert.setThresholdValue(BigDecimal.valueOf(60000));
    alert.setInitialPrice(bitcoin.getCurrentPrice());
    alert = priceAlertRepository.save(alert);

    // then
    mockMvc.perform(delete("/api/alerts/" + alert.getId())
                    .with(user(testUserDetails)))
            .andExpect(status().isNoContent());

    assertThat(priceAlertRepository.findById(alert.getId())).isEmpty();
}

@Test
void getAlertsForCoin_Success() throws Exception {
    // given
    PriceAlert alert1 = new PriceAlert();
    alert1.setUser(testUser);
    alert1.setCoin(bitcoin);
    alert1.setType(AlertType.PRICE_ABOVE);
    alert1.setStatus(AlertStatus.ACTIVE);
    alert1.setThresholdValue(BigDecimal.valueOf(60000));
    priceAlertRepository.save(alert1);

    PriceAlert alert2 = new PriceAlert();
    alert2.setUser(testUser);
    alert2.setCoin(bitcoin);
    alert2.setType(AlertType.PRICE_BELOW);
    alert2.setStatus(AlertStatus.TRIGGERED);
    alert2.setThresholdValue(BigDecimal.valueOf(40000));
    priceAlertRepository.save(alert2);

    // then
    mockMvc.perform(get("/api/alerts/coin/bitcoin")
                    .with(user(testUserDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].coinId").value("bitcoin"))
            .andExpect(jsonPath("$[1].coinId").value("bitcoin"));
}
}