package kosukeroku.token_radar.service;

import kosukeroku.token_radar.dto.CoinGeckoCoinDto;
import kosukeroku.token_radar.exception.CoinGeckoApiException;
import kosukeroku.token_radar.mapper.CoinMapper;
import kosukeroku.token_radar.repository.CoinRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Data
public class CoinGeckoService {

    private final WebClient webClient;

    // gets all info for top500 coins on coingecko
    public Mono<List<CoinGeckoCoinDto>> getTopCoins() {
        log.debug("Fetching top coins with extended data from CoinGecko API...");

        return Flux.range(1, 5)
                .delayElements(Duration.ofSeconds(30))
                .flatMap(page -> {
                    log.debug("Fetching page {} from CoinGecko", page);
                    return webClient.get()
                            .uri("/coins/markets?vs_currency=usd&per_page=100&page={page}&order=market_cap_desc&price_change_percentage=1h,24h,7d,30d&sparkline=true", page)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response -> {
                                log.error("CoinGecko API error for page {}: HTTP {}", page, response.statusCode());
                                return Mono.error(new CoinGeckoApiException("API error: " + response.statusCode()));
                            })
                            .bodyToFlux(CoinGeckoCoinDto.class)
                            .onErrorResume(throwable -> {
                                log.warn("Failed to fetch page {}, skipping: {}", page, throwable.getMessage());
                                return Flux.empty();
                            });
                })
                .collectList()
                .doOnSuccess(coins -> log.debug("Successfully fetched {} coins with extended data from CoinGecko", coins.size()));
    }

    // getting only coin prices
    public Mono<List<CoinGeckoCoinDto>> getCoinPrices(List<String> coinIds) {
        if (coinIds.isEmpty()) {
            return Mono.just(List.of());
        }

        String ids = String.join(",", coinIds);
        log.debug("Fetching extended prices for {} coins: {}", coinIds.size(), ids);

        return webClient.get()
                .uri("/coins/markets?vs_currency=usd&ids={ids}&price_change_percentage=1h,24h,7d,30d&sparkline=true", ids)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("CoinGecko API error for prices: HTTP {}", response.statusCode());
                    return Mono.error(new CoinGeckoApiException("Price API error: " + response.statusCode()));
                })
                .bodyToFlux(CoinGeckoCoinDto.class)
                .collectList()
                .doOnSuccess(prices -> log.debug("Successfully fetched extended prices for {} coins", prices.size()))
                .onErrorResume(throwable -> {
                    log.warn("Failed to fetch prices, returning empty list: {}", throwable.getMessage());
                    return Mono.just(List.of());
                });
    }
}
