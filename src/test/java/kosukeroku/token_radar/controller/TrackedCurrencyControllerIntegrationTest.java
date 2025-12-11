package kosukeroku.token_radar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kosukeroku.token_radar.dto.TrackedCurrencyRequestDto;
import kosukeroku.token_radar.dto.TrackedCurrencyResponseDto;
import kosukeroku.token_radar.security.UserDetailsImpl;
import kosukeroku.token_radar.service.TrackedCurrencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TrackedCurrencyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrackedCurrencyService trackedCurrencyService;

    private UserDetailsImpl createTestUserDetails() {
        return new UserDetailsImpl(
                1L, "testuser", "test@example.com",
                "password", List.of()
        );
    }

    private TrackedCurrencyResponseDto createTrackedCurrencyResponseDto(
            Long id, String coinId, String coinName, BigDecimal price) {
        TrackedCurrencyResponseDto dto = new TrackedCurrencyResponseDto();
        dto.setId(id);
        dto.setCoinId(coinId);
        dto.setCoinName(coinName);
        dto.setCurrentPrice(price);
        return dto;
    }

    @Test
    @WithMockUser
    void addTrackedCurrency_Success() throws Exception {
        // given
        TrackedCurrencyRequestDto request = new TrackedCurrencyRequestDto();
        request.setCoinId("bitcoin");

        TrackedCurrencyResponseDto response = createTrackedCurrencyResponseDto(
                1L, "bitcoin", "Bitcoin", BigDecimal.valueOf(50000)
        );

        when(trackedCurrencyService.addTrackedCurrency(anyLong(), any(TrackedCurrencyRequestDto.class)))
                .thenReturn(response);

        // then
        mockMvc.perform(post("/api/tracked-currencies")
                        .with(user(createTestUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinId").value("bitcoin"))
                .andExpect(jsonPath("$.coinName").value("Bitcoin"))
                .andExpect(jsonPath("$.currentPrice").value(50000));
    }

    @Test
    @WithMockUser
    void addTrackedCurrency_InvalidRequest_MissingCoinId() throws Exception {
        // given
        TrackedCurrencyRequestDto request = new TrackedCurrencyRequestDto();
        // coinId is null leads to validation error

        // then
        mockMvc.perform(post("/api/tracked-currencies")
                        .with(user(createTestUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertThat(responseBody).contains("timestamp", "status", "error", "message");
                    assertThat(responseBody).contains("400");
                });

        verify(trackedCurrencyService, never()).addTrackedCurrency(anyLong(), any());
    }

    @Test
    @WithMockUser
    void removeTrackedCurrency_Success() throws Exception {
        // given
        var result = mockMvc.perform(delete("/api/tracked-currencies/bitcoin")
                        .with(user(createTestUserDetails())))
                .andExpect(status().isNoContent())
                .andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEmpty();
        verify(trackedCurrencyService).removeTrackedCurrency(1L, "bitcoin");
    }

    @Test
    @WithMockUser
    void removeTrackedCurrency_WithUppercaseSymbol() throws Exception {
        mockMvc.perform(delete("/api/tracked-currencies/BTC")
                        .with(user(createTestUserDetails())))
                .andExpect(status().isNoContent());

        verify(trackedCurrencyService).removeTrackedCurrency(1L, "BTC");
    }

    @Test
    @WithMockUser
    void getUserTrackedCurrencies_Success() throws Exception {
        // given
        TrackedCurrencyResponseDto bitcoin = createTrackedCurrencyResponseDto(
                1L, "bitcoin", "Bitcoin", BigDecimal.valueOf(50000)
        );

        TrackedCurrencyResponseDto ethereum = createTrackedCurrencyResponseDto(
                2L, "ethereum", "Ethereum", BigDecimal.valueOf(3000)
        );

        when(trackedCurrencyService.getUserTrackedCurrencies(1L))
                .thenReturn(List.of(bitcoin, ethereum));

        // then
        mockMvc.perform(get("/api/tracked-currencies")
                        .with(user(createTestUserDetails())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].coinId").value("bitcoin"))
                .andExpect(jsonPath("$[0].coinName").value("Bitcoin"))
                .andExpect(jsonPath("$[1].coinId").value("ethereum"))
                .andExpect(jsonPath("$[1].coinName").value("Ethereum"))
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    assertThat(json).contains("bitcoin", "Bitcoin", "ethereum", "Ethereum");
                });
    }

    @Test
    @WithMockUser
    void getUserTrackedCurrencies_EmptyList() throws Exception {
        // given
        when(trackedCurrencyService.getUserTrackedCurrencies(1L))
                .thenReturn(List.of());

        // then
        mockMvc.perform(get("/api/tracked-currencies")
                        .with(user(createTestUserDetails())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    assertThat(json).isEqualTo("[]");
                });
    }

    @Test
    void addTrackedCurrency_Unauthorized() throws Exception {
        // given
        TrackedCurrencyRequestDto request = new TrackedCurrencyRequestDto();
        request.setCoinId("bitcoin");

        // then
        mockMvc.perform(post("/api/tracked-currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertThat(response).isNotNull();
                });

        verify(trackedCurrencyService, never()).addTrackedCurrency(anyLong(), any());
    }

    @Test
    void removeTrackedCurrency_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/tracked-currencies/bitcoin"))
                .andExpect(status().isForbidden());

        verify(trackedCurrencyService, never()).removeTrackedCurrency(anyLong(), any());
    }

    @Test
    void getUserTrackedCurrencies_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/tracked-currencies"))
                .andExpect(status().isForbidden());

        verify(trackedCurrencyService, never()).getUserTrackedCurrencies(anyLong());
    }

    @Test
    @WithMockUser
    void addTrackedCurrency_ServiceThrowsException() throws Exception {
        // given
        TrackedCurrencyRequestDto request = new TrackedCurrencyRequestDto();
        request.setCoinId("invalid-coin");

        when(trackedCurrencyService.addTrackedCurrency(anyLong(), any()))
                .thenThrow(new RuntimeException("Coin not found"));

        // then
        mockMvc.perform(post("/api/tracked-currencies")
                        .with(user(createTestUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertThat(response).contains("timestamp", "status", "error");
                });
    }

    @Test
    @WithMockUser
    void removeTrackedCurrency_WithPathVariableContainingSpecialCharacters() throws Exception {
        mockMvc.perform(delete("/api/tracked-currencies/usd-coin")
                        .with(user(createTestUserDetails())))
                .andExpect(status().isNoContent());

        verify(trackedCurrencyService).removeTrackedCurrency(1L, "usd-coin");
    }

    @Test
    @WithMockUser
    void addTrackedCurrency_WithWrongContentType() throws Exception {
        // given
        TrackedCurrencyRequestDto request = new TrackedCurrencyRequestDto();
        request.setCoinId("bitcoin");

        // then
        mockMvc.perform(post("/api/tracked-currencies")
                        .with(user(createTestUserDetails()))
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text"))
                .andExpect(status().isUnsupportedMediaType());

        verify(trackedCurrencyService, never()).addTrackedCurrency(anyLong(), any());
    }

    @Test
    @WithMockUser
    void getUserTrackedCurrencies_SingleItem() throws Exception {
        // given
        TrackedCurrencyResponseDto bitcoin = createTrackedCurrencyResponseDto(
                1L, "bitcoin", "Bitcoin", BigDecimal.valueOf(50000)
        );

        when(trackedCurrencyService.getUserTrackedCurrencies(1L))
                .thenReturn(List.of(bitcoin));

        // then
        mockMvc.perform(get("/api/tracked-currencies")
                        .with(user(createTestUserDetails())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    assertThat(json).contains("bitcoin", "Bitcoin");
                });
    }
}