package kosukeroku.token_radar.controller;

import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coins")
public class CoinController {

    private final CoinService coinService;

    @GetMapping("/dashboard")
    public Page<Coin> getDashboard(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("marketCapRank").ascending());
        return coinService.getDashboard(pageable);
    }

    @GetMapping("/search")
    public List<Coin> searchCoins(@RequestParam String query) {
        return coinService.searchCoins(query);
    }
}