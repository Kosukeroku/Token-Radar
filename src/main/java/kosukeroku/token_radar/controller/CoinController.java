package kosukeroku.token_radar.controller;

import kosukeroku.token_radar.dto.CoinResponseDto;
import kosukeroku.token_radar.mapper.CoinMapper;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coins")
public class CoinController {

    private final CoinService coinService;
    private final CoinMapper coinMapper;

    @GetMapping("/dashboard")
    public Page<CoinResponseDto> getDashboard(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("marketCapRank").ascending());
        Page<Coin> coinPage = coinService.getDashboard(pageable);

        return coinPage.map(coinMapper::toResponseDto);
    }

    @GetMapping("/search")
    public List<CoinResponseDto> searchCoins(@RequestParam String query) {
        List<Coin> coins = coinService.searchCoins(query);
        return coins.stream()
                .map(coinMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}