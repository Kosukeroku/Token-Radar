package kosukeroku.token_radar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kosukeroku.token_radar.dto.PriceAlertRequestDto;
import kosukeroku.token_radar.dto.PriceAlertResponseDto;
import kosukeroku.token_radar.dto.AlertStatsDto;
import kosukeroku.token_radar.model.enums.AlertStatus;
import kosukeroku.token_radar.model.enums.AlertType;
import kosukeroku.token_radar.security.UserDetailsImpl;
import kosukeroku.token_radar.service.PriceAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@ExtendWith(MockitoExtension.class)
class PriceAlertControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PriceAlertService priceAlertService;

    private UserDetailsImpl testUserDetails;
    private PriceAlertResponseDto testAlertResponse;
    private PriceAlertRequestDto testAlertRequest;

    @BeforeEach
    void setUp() {
        testUserDetails = new UserDetailsImpl(
                1L, "testuser", "test@example.com",
                "password", List.of()
        );

        testAlertResponse = createTestAlertResponse();

        testAlertRequest = new PriceAlertRequestDto();
        testAlertRequest.setCoinId("bitcoin");
        testAlertRequest.setType(AlertType.PRICE_ABOVE);
        testAlertRequest.setThresholdValue(BigDecimal.valueOf(60000));
    }

    @Test
    void createAlert_Success() throws Exception {
        // given
        when(priceAlertService.createAlert(anyLong(), any(PriceAlertRequestDto.class)))
                .thenReturn(testAlertResponse);

        // then
        mockMvc.perform(post("/api/alerts")
                        .with(user(testUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAlertRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.coinId").value("bitcoin"))
                .andExpect(jsonPath("$.type").value("price_above"))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.thresholdValue").value(60000));
    }

    @Test
    void createAlert_InvalidRequest() throws Exception {
        // given
        PriceAlertRequestDto invalidRequest = new PriceAlertRequestDto();
        // missing required fields

        // then
        mockMvc.perform(post("/api/alerts")
                        .with(user(testUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserAlerts_Success() throws Exception {
        // given
        List<PriceAlertResponseDto> alerts = List.of(testAlertResponse);
        when(priceAlertService.getUserAlerts(anyLong())).thenReturn(alerts);

        // then
        mockMvc.perform(get("/api/alerts")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].coinId").value("bitcoin"));
    }

    @Test
    void getAlertsByStatus_Success() throws Exception {
        // given
        List<PriceAlertResponseDto> alerts = List.of(testAlertResponse);
        when(priceAlertService.getUserAlertsByStatus(anyLong(), any(AlertStatus.class)))
                .thenReturn(alerts);

        // then
        mockMvc.perform(get("/api/alerts/status/ACTIVE")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAlertStats_Success() throws Exception {
        // given
        AlertStatsDto stats = new AlertStatsDto(10L, 5L, 3L, 2L, 3L);
        when(priceAlertService.getAlertStats(anyLong())).thenReturn(stats);

        // then
        mockMvc.perform(get("/api/alerts/stats")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAlerts").value(10))
                .andExpect(jsonPath("$.activeAlerts").value(5))
                .andExpect(jsonPath("$.triggeredAlerts").value(3));
    }

    @Test
    void getUnreadCount_Success() throws Exception {
        // given
        when(priceAlertService.getUnreadCount(anyLong())).thenReturn(3L);

        // then
        mockMvc.perform(get("/api/alerts/unread-count")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void markAsRead_Success() throws Exception {
        // given
        PriceAlertResponseDto updatedAlert = testAlertResponse;
        updatedAlert.setStatus(AlertStatus.READ);

        when(priceAlertService.markAsRead(anyLong(), anyLong()))
                .thenReturn(updatedAlert);

        // then
        mockMvc.perform(patch("/api/alerts/1/read")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("read"));
    }

    @Test
    void markAllAsRead_Success() throws Exception {
        // then
        mockMvc.perform(post("/api/alerts/read-all")
                        .with(user(testUserDetails)))
                .andExpect(status().isNoContent());
    }

    @Test
    void clearReadAlerts_Success() throws Exception {
        // then
        mockMvc.perform(post("/api/alerts/clear-read")
                        .with(user(testUserDetails)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAlert_Success() throws Exception {
        // then
        mockMvc.perform(delete("/api/alerts/1")
                        .with(user(testUserDetails)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAlertsForCoin_Success() throws Exception {
        // given
        List<PriceAlertResponseDto> alerts = List.of(testAlertResponse);
        when(priceAlertService.getUserAlertsForCoin(anyLong(), anyString()))
                .thenReturn(alerts);

        // then
        mockMvc.perform(get("/api/alerts/coin/bitcoin")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getUserNotifications_Success() throws Exception {
        // given
        PriceAlertResponseDto triggeredAlert = createTestAlertResponse();
        triggeredAlert.setStatus(AlertStatus.TRIGGERED);
        triggeredAlert.setNotificationMessage("Price reached target!");

        List<PriceAlertResponseDto> notifications = List.of(triggeredAlert);
        when(priceAlertService.getUserNotifications(anyLong()))
                .thenReturn(notifications);

        // then
        mockMvc.perform(get("/api/alerts/notifications")
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].notificationMessage").value("Price reached target!"));
    }

    @Test
    void createAlert_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAlertRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAlerts_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isForbidden());
    }

    private PriceAlertResponseDto createTestAlertResponse() {
        PriceAlertResponseDto alert = new PriceAlertResponseDto();
        alert.setId(1L);
        alert.setCoinId("bitcoin");
        alert.setCoinName("Bitcoin");
        alert.setCoinSymbol("BTC");
        alert.setCoinImageUrl("http://example.com/bitcoin.png");
        alert.setType(AlertType.PRICE_ABOVE);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setThresholdValue(BigDecimal.valueOf(60000));
        alert.setCurrentPrice(BigDecimal.valueOf(55000));
        alert.setInitialPrice(BigDecimal.valueOf(50000));
        alert.setCreatedAt(LocalDateTime.now());
        alert.setIsRead(false);
        return alert;
    }
}