package kosukeroku.token_radar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kosukeroku.token_radar.dto.UserProfileDto;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.model.TrackedCurrency;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.repository.CoinRepository;
import kosukeroku.token_radar.repository.TrackedCurrencyRepository;
import kosukeroku.token_radar.repository.UserRepository;
import kosukeroku.token_radar.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private TrackedCurrencyRepository trackedCurrencyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Coin bitcoin;
    private Coin ethereum;
    private UserDetailsImpl testUserDetails;

    @BeforeEach
    void setUp() {
        // clean up database
        trackedCurrencyRepository.deleteAll();
        coinRepository.deleteAll();
        userRepository.deleteAll();

        // create test user
        testUser = new User();
        testUser.setUsername("integrationuser");
        testUser.setEmail("integration@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // create test coins
        bitcoin = createCoin("bitcoin", "Bitcoin", "BTC", 1, new BigDecimal("50000"));
        ethereum = createCoin("ethereum", "Ethereum", "ETH", 2, new BigDecimal("3000"));

        coinRepository.saveAll(List.of(bitcoin, ethereum));

        // create user details for authentication
        testUserDetails = new UserDetailsImpl(
                testUser.getId(),
                testUser.getUsername(),
                testUser.getEmail(),
                testUser.getPassword(),
                List.of()
        );
    }

    @Test
    void getProfile_Success_WithNoTrackedCurrencies() throws Exception {
        mockMvc.perform(get("/api/profile")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    String responseJson = result.getResponse().getContentAsString();
                    UserProfileDto profile = objectMapper.readValue(responseJson, UserProfileDto.class);

                    assertThat(profile)
                            .isNotNull()
                            .satisfies(p -> {
                                assertThat(p.getId()).isEqualTo(testUser.getId());
                                assertThat(p.getUsername()).isEqualTo("integrationuser");
                                assertThat(p.getEmail()).isEqualTo("integration@example.com");
                                assertThat(p.getTrackedCurrencies()).isEmpty();
                                assertThat(p.getTrackedCount()).isZero();
                                assertThat(p.getCreatedAt()).isNotNull();
                            });
                });
    }

    @Test
    void getProfile_Success_WithTrackedCurrencies() throws Exception {
        // given
        TrackedCurrency trackedBitcoin = createTrackedCurrency(testUser, bitcoin);
        TrackedCurrency trackedEthereum = createTrackedCurrency(testUser, ethereum);
        trackedCurrencyRepository.saveAll(List.of(trackedBitcoin, trackedEthereum));

        // then
        mockMvc.perform(get("/api/profile")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.trackedCount").value(2))
                .andExpect(jsonPath("$.trackedCurrencies").isArray())
                .andExpect(jsonPath("$.trackedCurrencies.length()").value(2))
                .andExpect(result -> {
                    String responseJson = result.getResponse().getContentAsString();

                    // parsing and verifying the response
                    UserProfileDto profile = objectMapper.readValue(responseJson, UserProfileDto.class);

                    assertThat(profile.getTrackedCurrencies())
                            .hasSize(2)
                            .extracting("coinId")
                            .containsExactly("bitcoin", "ethereum");

                    assertThat(profile.getTrackedCurrencies())
                            .extracting("coinName")
                            .containsExactly("Bitcoin", "Ethereum");

                    // verifying sorting by market cap rank
                    assertThat(profile.getTrackedCurrencies().get(0).getCoinId()).isEqualTo("bitcoin"); // rank 1
                    assertThat(profile.getTrackedCurrencies().get(1).getCoinId()).isEqualTo("ethereum"); // rank 2
                });
    }

    @Test
    void getProfile_Success_WithMultipleUsers() throws Exception {
        // given
        User secondUser = new User();
        secondUser.setUsername("seconduser");
        secondUser.setEmail("second@example.com");
        secondUser.setPassword(passwordEncoder.encode("password123"));
        secondUser.setCreatedAt(LocalDateTime.now());
        secondUser = userRepository.save(secondUser);

        // first user tracks bitcoin
        TrackedCurrency trackedBitcoin = createTrackedCurrency(testUser, bitcoin);
        trackedCurrencyRepository.save(trackedBitcoin);

        // second user tracks ethereum
        TrackedCurrency trackedEthereum = createTrackedCurrency(secondUser, ethereum);
        trackedCurrencyRepository.save(trackedEthereum);

        UserDetailsImpl secondUserDetails = new UserDetailsImpl(
                secondUser.getId(),
                secondUser.getUsername(),
                secondUser.getEmail(),
                secondUser.getPassword(),
                List.of()
        );

        // then: first user's profile
        mockMvc.perform(get("/api/profile")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseJson = result.getResponse().getContentAsString();
                    UserProfileDto profile = objectMapper.readValue(responseJson, UserProfileDto.class);

                    assertThat(profile.getTrackedCurrencies())
                            .hasSize(1)
                            .extracting("coinId")
                            .containsOnly("bitcoin");

                    assertThat(profile.getTrackedCount()).isEqualTo(1L);
                });

        // then: second user's profile
        mockMvc.perform(get("/api/profile")
                        .with(user(secondUserDetails)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseJson = result.getResponse().getContentAsString();
                    UserProfileDto profile = objectMapper.readValue(responseJson, UserProfileDto.class);

                    assertThat(profile.getTrackedCurrencies())
                            .hasSize(1)
                            .extracting("coinId")
                            .containsOnly("ethereum");
                });
    }

    @Test
    void getProfile_Unauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isForbidden()) // Spring Security returns 403 for unauthenticated requests
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertThat(result.getResponse().getStatus()).isEqualTo(403);
                });
    }


    @Test
    void getProfile_Success_AfterAddingTrackedCurrency() throws Exception {
        // given (initially 0 tracked currencies)
        mockMvc.perform(get("/api/profile")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseJson = result.getResponse().getContentAsString();
                    UserProfileDto profile = objectMapper.readValue(responseJson, UserProfileDto.class);
                    assertThat(profile.getTrackedCount()).isZero();
                });

        // adding a tracked currency
        TrackedCurrency trackedBitcoin = createTrackedCurrency(testUser, bitcoin);
        trackedCurrencyRepository.save(trackedBitcoin);

        // then
        mockMvc.perform(get("/api/profile")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseJson = result.getResponse().getContentAsString();
                    UserProfileDto profile = objectMapper.readValue(responseJson, UserProfileDto.class);

                    assertThat(profile.getTrackedCount()).isEqualTo(1L);
                    assertThat(profile.getTrackedCurrencies())
                            .hasSize(1)
                            .extracting("coinId")
                            .containsOnly("bitcoin");
                });
    }

    @Test
    void getProfile_Success_AfterRemovingTrackedCurrency() throws Exception {
        // given
        TrackedCurrency trackedBitcoin = createTrackedCurrency(testUser, bitcoin);
        TrackedCurrency trackedEthereum = createTrackedCurrency(testUser, ethereum);
        trackedCurrencyRepository.saveAll(List.of(trackedBitcoin, trackedEthereum));

        mockMvc.perform(get("/api/profile")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseJson = result.getResponse().getContentAsString();
                    UserProfileDto profile = objectMapper.readValue(responseJson, UserProfileDto.class);
                    assertThat(profile.getTrackedCount()).isEqualTo(2L);
                });

        // remove one tracked currency
        trackedCurrencyRepository.delete(trackedEthereum);

        // then
        mockMvc.perform(get("/api/profile")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseJson = result.getResponse().getContentAsString();
                    UserProfileDto profile = objectMapper.readValue(responseJson, UserProfileDto.class);

                    assertThat(profile.getTrackedCount()).isEqualTo(1L);
                    assertThat(profile.getTrackedCurrencies())
                            .hasSize(1)
                            .extracting("coinId")
                            .containsOnly("bitcoin");
                });
    }

    private Coin createCoin(String id, String name, String symbol, int rank, BigDecimal price) {
        Coin coin = new Coin();
        coin.setId(id);
        coin.setName(name);
        coin.setSymbol(symbol);
        coin.setMarketCapRank(rank);
        coin.setCurrentPrice(price);
        coin.setActive(true);
        coin.setImageUrl("http://example.com/" + id + ".png");
        coin.setMarketCap(price.multiply(new BigDecimal("1000000")));
        coin.setTotalVolume(price.multiply(new BigDecimal("100000")));
        coin.setPriceChange24h(100.0);
        coin.setPriceChangePercentage24h(2.5);
        return coin;
    }

    private TrackedCurrency createTrackedCurrency(User user, Coin coin) {
        TrackedCurrency trackedCurrency = new TrackedCurrency();
        trackedCurrency.setUser(user);
        trackedCurrency.setCoin(coin);
        trackedCurrency.setAddedAt(LocalDateTime.now());
        return trackedCurrency;
    }
}