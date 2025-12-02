package kosukeroku.token_radar.service;

import kosukeroku.token_radar.exception.CoinNotFoundException;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoinService {

    private final CoinRepository coinRepository;

    public List<Coin> getAllCoins() {
        return coinRepository.findAll();
    }

    public Page<Coin> getDashboard(Pageable pageable) {
        log.debug("Fetching dashboard page: {}", pageable);
        return coinRepository.findByActiveTrue(pageable);
    }

    public List<Coin> searchCoins(String query) {
        List<Coin> allCoins = getAllCoins();
        return allCoins.stream()
                .filter(coin -> coin.getName().toLowerCase().contains(query.toLowerCase()) ||
                        coin.getSymbol().toLowerCase().contains(query.toLowerCase()))
                .sorted(Comparator.comparing(Coin::getMarketCapRank))
                .collect(Collectors.toList());
    }

    public Coin getCoinById(String coinId) {
        return coinRepository.findById(coinId)
                .orElseThrow(() -> new CoinNotFoundException(coinId));
    }
}