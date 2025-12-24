package kosukeroku.token_radar.controller;

import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.repository.CoinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CoinControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CoinRepository coinRepository;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private kosukeroku.token_radar.service.kafka.KafkaConsumerService kafkaConsumerService;

    @BeforeEach
    void setUp() {
        coinRepository.deleteAll();
    }

    @Test
    void getDashboard_ShouldReturnPaginatedCoins_WhenCoinsExist() throws Exception {
        // given
        Coin bitcoin = createCoin("bitcoin", "Bitcoin", "btc", 1, new BigDecimal("100000"));
        Coin ethereum = createCoin("ethereum", "Ethereum", "eth", 2, new BigDecimal("3000"));
        Coin cardano = createCoin("cardano", "Cardano", "ada", 3, new BigDecimal("1.5"));

        coinRepository.saveAll(List.of(bitcoin, ethereum, cardano));

        // then
        mockMvc.perform(get("/api/coins/dashboard")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is("bitcoin")))
                .andExpect(jsonPath("$.content[1].id", is("ethereum")))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    void getDashboard_ShouldReturnDefaultPagination_WhenNoParamsProvided() throws Exception {
        // given
        Coin bitcoin = createCoin("bitcoin", "Bitcoin", "btc", 1, new BigDecimal("100000"));
        Coin ethereum = createCoin("ethereum", "Ethereum", "eth", 2, new BigDecimal("3000"));

        coinRepository.saveAll(List.of(bitcoin, ethereum));

        // then
        mockMvc.perform(get("/api/coins/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.size", is(20))) // default size
                .andExpect(jsonPath("$.number", is(0))); // default page
    }

    @Test
    void getDashboard_ShouldReturnEmptyPage_WhenNoCoinsExist() throws Exception {
        // then
        mockMvc.perform(get("/api/coins/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", empty()))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)));
    }

    @Test
    void getDashboard_ShouldReturnOnlyActiveCoins() throws Exception {
        // given
        Coin activeCoin = createCoin("bitcoin", "Bitcoin", "btc", 1, new BigDecimal("100000"));
        activeCoin.setActive(true);

        Coin inactiveCoin = createCoin("inactive", "Inactive Coin", "inc", 100, new BigDecimal("1"));
        inactiveCoin.setActive(false);

        coinRepository.saveAll(List.of(activeCoin, inactiveCoin));

        // then
        mockMvc.perform(get("/api/coins/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is("bitcoin")))
                .andExpect(jsonPath("$.content[0].id", is("bitcoin")));
    }

    @Test
    void getDashboard_ShouldReturnCoinsSortedByMarketCapRank() throws Exception {
        // given
        Coin thirdCoin = createCoin("cardano", "Cardano", "ada", 3, new BigDecimal("1.5"));
        Coin firstCoin = createCoin("bitcoin", "Bitcoin", "btc", 1, new BigDecimal("100000"));
        Coin secondCoin = createCoin("ethereum", "Ethereum", "eth", 2, new BigDecimal("3000"));

        coinRepository.saveAll(List.of(thirdCoin, firstCoin, secondCoin));

        // then
        mockMvc.perform(get("/api/coins/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].marketCapRank", is(1)))
                .andExpect(jsonPath("$.content[1].marketCapRank", is(2)))
                .andExpect(jsonPath("$.content[2].marketCapRank", is(3)));
    }

    @Test
    void getDashboard_ShouldHandleLargePageNumbersGracefully() throws Exception {
        // given
        Coin bitcoin = createCoin("bitcoin", "Bitcoin", "btc", 1, new BigDecimal("100000"));
        coinRepository.save(bitcoin);

        // then - requesting page that doesn't exist should return empty content
        mockMvc.perform(get("/api/coins/dashboard")
                        .param("page", "100")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", empty()))
                .andExpect(jsonPath("$.totalPages", is(1))); // only one page exists
    }

    private Coin createCoin(String id, String name, String symbol, int rank, BigDecimal price) {
        Coin coin = new Coin();
        coin.setId(id);
        coin.setName(name);
        coin.setSymbol(symbol);
        coin.setMarketCapRank(rank);
        coin.setCurrentPrice(price);
        coin.setActive(true);
        coin.setMarketCap(price.multiply(new BigDecimal("1000000")));
        coin.setTotalVolume(price.multiply(new BigDecimal("100000")));
        coin.setPriceChange24h(100.0);
        coin.setPriceChangePercentage24h(2.5);
        return coin;
    }
}