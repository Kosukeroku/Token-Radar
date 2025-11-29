package kosukeroku.token_radar.service;

import kosukeroku.token_radar.dto.CoinGeckoCoinDto;
import kosukeroku.token_radar.mapper.CoinMapper;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.format.DateTimeFormatter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoinSyncService {

    private final CoinGeckoService coinGeckoService;
    private final CoinMapper coinMapper;
    private final CoinRepository coinRepository;

    // updating all (names, icons, prices) info once a day
    @CacheEvict(value = {"coins", "coin-prices"}, allEntries = true)
    @Scheduled(cron = "0 0 3 * * ?")
    public void syncAllCoinData() {
        log.info("Starting full coin data synchronization...");

        // deleting inactive coins before synchronization
        coinRepository.deleteByActiveFalse();

        // fetching and saving top 500 coins
        coinGeckoService.getTopCoins()
                .flatMap(this::saveCoins)
                .doOnError(error -> log.error("Full coin synchronization failed: {}", error.getMessage()))
                .subscribe();
    }

    // updates only prices every 10 minutes
    @CacheEvict(value = "coin-prices", allEntries = true)
    @Scheduled(fixedRate = 600000)
    public void syncPricesOnly() {
        log.info("Starting price-only synchronization for all coins...");

        // getting all active coins' IDs from the database
        List<String> allCoinIds = coinRepository.findAllActiveCoinIds();

        if (allCoinIds.isEmpty()) {
            log.warn("No active coins found for price sync");
            return;
        }

        // splitting into batches of 100, which is coingecko's limit
        List<List<String>> batches = partitionList(allCoinIds, 100);

        // processing batches with a 20 seconds delay between them
        Flux.fromIterable(batches)
                .delayElements(Duration.ofSeconds(20))
                .flatMap(batch -> coinGeckoService.getCoinPrices(batch)
                        .flatMap(this::updatePrices)// updating the prices in the database
                        .onErrorResume(error -> {
                            log.warn("Failed to fetch price batch, skipping: {}", error.getMessage());
                            return Mono.empty(); // skipping this batch in case of an error
                        })
                )
                .subscribe(
                        null, // we don't need to process every successful result individually, all the necessary logic here is calling the updatePrices method
                        error -> log.error("Price synchronization failed: {}", error.getMessage()), // on error
                        () -> log.info("Price synchronization completed for all {} coins", allCoinIds.size()) // on success
                );
    }

    private List<List<String>> partitionList(List<String> list, int size) {
        int totalBatches = (int) Math.ceil((double) list.size() / size);

        return IntStream.range(0, totalBatches)
                .mapToObj(i -> list.subList(i * size, Math.min(list.size(), (i + 1) * size)))
                .collect(Collectors.toList());
    }

    private Mono<Void> saveCoins(List<CoinGeckoCoinDto> coins) {
        return Mono.fromRunnable(() -> {
            List<Coin> entities = coins.stream()
                    .map(dto -> {
                        Coin coin = coinMapper.toEntity(dto);
                        // manually processing sparkline and ath date fields
                        if (dto.getSparkline_in_7d() != null && dto.getSparkline_in_7d().getPrice() != null) {
                            try {
                                coin.setSparklineData(new ObjectMapper().writeValueAsString(dto.getSparkline_in_7d().getPrice()));
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                        if (dto.getAth_date() != null) {
                            try {
                                coin.setAthDate(LocalDateTime.parse(dto.getAth_date(), DateTimeFormatter.ISO_DATE_TIME));
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                        return coin;
                    })
                    .collect(Collectors.toList());
            coinRepository.saveAll(entities);
            log.info("Full sync completed. Updated {} coins", entities.size());
        });
    }

    private Mono<Void> updatePrices(List<CoinGeckoCoinDto> priceDtos) {
        return Mono.fromRunnable(() -> {
            if (!priceDtos.isEmpty()) {
                priceDtos.forEach(dto -> {
                    coinRepository.findById(dto.getId()).ifPresent(coin -> {
                        // prices, marketcap, volume etc.
                        coin.setCurrentPrice(dto.getCurrent_price());
                        coin.setPriceChange24h(dto.getPrice_change_24h());
                        coin.setPriceChangePercentage24h(dto.getPrice_change_percentage_24h());
                        coin.setMarketCap(dto.getMarket_cap());
                        coin.setTotalVolume(dto.getTotal_volume());
                        coin.setPriceChangePercentage1h(dto.getPrice_change_percentage_1h_in_currency());
                        coin.setPriceChangePercentage7d(dto.getPrice_change_percentage_7d_in_currency());
                        coin.setPriceChangePercentage30d(dto.getPrice_change_percentage_30d_in_currency());
                        coin.setHigh24h(dto.getHigh_24h());
                        coin.setLow24h(dto.getLow_24h());
                        coin.setAth(dto.getAth());
                        coin.setAthChangePercentage(dto.getAth_change_percentage());

                        // sparkline data
                        if (dto.getSparkline_in_7d() != null && dto.getSparkline_in_7d().getPrice() != null) {
                            try {
                                coin.setSparklineData(new ObjectMapper().writeValueAsString(dto.getSparkline_in_7d().getPrice()));
                            } catch (Exception e) {
                                log.warn("Failed to serialize sparkline data for coin {}", dto.getId());
                            }
                        }

                        // ATH data
                        if (dto.getAth_date() != null) {
                            try {
                                coin.setAthDate(LocalDateTime.parse(dto.getAth_date(), DateTimeFormatter.ISO_DATE_TIME));
                            } catch (Exception e) {
                                log.warn("Failed to parse ATH date for coin {}", dto.getId());
                            }
                        }

                        coin.setLastUpdated(LocalDateTime.now());
                        coinRepository.save(coin);
                    });
                });
                log.debug("Updated extended prices for {} coins", priceDtos.size());
            }
        });
    }
}
