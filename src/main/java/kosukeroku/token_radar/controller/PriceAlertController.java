package kosukeroku.token_radar.controller;

import jakarta.validation.Valid;
import kosukeroku.token_radar.dto.*;
import kosukeroku.token_radar.model.PriceAlert;
import kosukeroku.token_radar.model.enums.AlertStatus;
import kosukeroku.token_radar.security.UserDetailsImpl;
import kosukeroku.token_radar.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    @PostMapping
    public ResponseEntity<PriceAlertResponseDto> createAlert(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PriceAlertRequestDto request) {
        PriceAlertResponseDto response = priceAlertService.createAlert(
                userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PriceAlertResponseDto>> getUserAlerts(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<PriceAlertResponseDto> alerts = priceAlertService.getUserAlerts(
                userDetails.getId());
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PriceAlertResponseDto>> getAlertsByStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable AlertStatus status) {
        List<PriceAlertResponseDto> alerts = priceAlertService.getUserAlertsByStatus(
                userDetails.getId(), status);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/stats")
    public ResponseEntity<AlertStatsDto> getAlertStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AlertStatsDto stats = priceAlertService.getAlertStats(userDetails.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long count = priceAlertService.getUnreadCount(userDetails.getId());
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{alertId}/read")
    public ResponseEntity<PriceAlertResponseDto> markAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long alertId) {
        PriceAlertResponseDto updated = priceAlertService.markAsRead(
                userDetails.getId(), alertId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        priceAlertService.markAllAsRead(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clear-read")
    public ResponseEntity<Void> clearReadAlerts(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        priceAlertService.clearReadAlerts(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deleteAlert(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long alertId) {
        priceAlertService.deleteAlert(userDetails.getId(), alertId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/coin/{coinId}")
    public ResponseEntity<List<PriceAlertResponseDto>> getAlertsForCoin(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String coinId) {
        List<PriceAlertResponseDto> alerts = priceAlertService.getUserAlertsForCoin(
                userDetails.getId(), coinId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<PriceAlertResponseDto>> getUserNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<PriceAlertResponseDto> notifications = priceAlertService.getUserNotifications(
                userDetails.getId());
        return ResponseEntity.ok(notifications);
    }
}