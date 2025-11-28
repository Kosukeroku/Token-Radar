package kosukeroku.token_radar.controller;

import jakarta.validation.Valid;
import kosukeroku.token_radar.dto.TrackedCurrencyRequestDto;
import kosukeroku.token_radar.dto.TrackedCurrencyResponseDto;
import kosukeroku.token_radar.security.UserDetailsImpl;
import kosukeroku.token_radar.service.TrackedCurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tracked-currencies")
public class TrackedCurrencyController {

    private final TrackedCurrencyService trackedCurrencyService;

    @PostMapping
    public ResponseEntity<TrackedCurrencyResponseDto> addTrackedCurrency(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody TrackedCurrencyRequestDto request) {

        TrackedCurrencyResponseDto response = trackedCurrencyService.addTrackedCurrency(
                userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{coinId}")
    public ResponseEntity<Void> removeTrackedCurrency(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String coinId) {

        trackedCurrencyService.removeTrackedCurrency(userDetails.getId(), coinId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TrackedCurrencyResponseDto>> getUserTrackedCurrencies(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<TrackedCurrencyResponseDto> trackedCurrencies =
                trackedCurrencyService.getUserTrackedCurrencies(userDetails.getId());
        return ResponseEntity.ok(trackedCurrencies);
    }
}