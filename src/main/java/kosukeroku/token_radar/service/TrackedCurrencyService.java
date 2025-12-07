package kosukeroku.token_radar.service;

import kosukeroku.token_radar.dto.TrackedCurrencyRequestDto;
import kosukeroku.token_radar.dto.TrackedCurrencyResponseDto;
import kosukeroku.token_radar.exception.CoinNotFoundException;
import kosukeroku.token_radar.exception.TrackedCurrencyAlreadyExistsException;
import kosukeroku.token_radar.exception.TrackedCurrencyNotFoundException;
import kosukeroku.token_radar.model.Coin;
import kosukeroku.token_radar.model.TrackedCurrency;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.repository.CoinRepository;
import kosukeroku.token_radar.repository.TrackedCurrencyRepository;
import kosukeroku.token_radar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrackedCurrencyService {

    private final TrackedCurrencyRepository trackedCurrencyRepository;
    private final CoinRepository coinRepository;
    private final UserRepository userRepository;

    @Transactional
    public TrackedCurrencyResponseDto addTrackedCurrency(Long userId, TrackedCurrencyRequestDto request) {
        log.info("Adding tracked currency for user {}: {}", userId, request.getCoinId());

        // checking if the coin exists
        Coin coin = coinRepository.findById(request.getCoinId())
                .orElseThrow(() -> new CoinNotFoundException(request.getCoinId()));

        // checking if the user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        // checking if the coin is already tracked
        if (trackedCurrencyRepository.existsByUserIdAndCoinId(userId, request.getCoinId())) {
            throw new TrackedCurrencyAlreadyExistsException(request.getCoinId());
        }

        // creating TrackedCurrency
        TrackedCurrency trackedCurrency = new TrackedCurrency();
        trackedCurrency.setUser(user);
        trackedCurrency.setCoin(coin);

        TrackedCurrency saved = trackedCurrencyRepository.save(trackedCurrency);
        return mapToResponseDto(saved);
    }

    @Transactional
    public void removeTrackedCurrency(Long userId, String coinId) {
        log.info("Removing tracked currency for user {}: {}", userId, coinId);

        TrackedCurrency trackedCurrency = trackedCurrencyRepository.findByUserIdAndCoinId(userId, coinId)
                .orElseThrow(() -> new TrackedCurrencyNotFoundException(userId, coinId));

        trackedCurrencyRepository.delete(trackedCurrency);
    }

    @Transactional(readOnly = true)
    public List<TrackedCurrencyResponseDto> getUserTrackedCurrencies(Long userId) {
        log.debug("Fetching tracked currencies for user: {}", userId);

        return trackedCurrencyRepository.findByUserIdOrderByCoinMarketCapRankAsc(userId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Long getUserTrackedCount(Long userId) {
        return trackedCurrencyRepository.countByUserId(userId);
    }

    private TrackedCurrencyResponseDto mapToResponseDto(TrackedCurrency trackedCurrency) {
        TrackedCurrencyResponseDto dto = new TrackedCurrencyResponseDto();
        dto.setId(trackedCurrency.getId());
        dto.setCoinId(trackedCurrency.getCoin().getId());
        dto.setCoinName(trackedCurrency.getCoin().getName());
        dto.setCoinSymbol(trackedCurrency.getCoin().getSymbol());
        dto.setCoinImageUrl(trackedCurrency.getCoin().getImageUrl());
        dto.setAddedAt(trackedCurrency.getAddedAt());
        dto.setCurrentPrice(trackedCurrency.getCoin().getCurrentPrice());
        dto.setPriceChange24h(trackedCurrency.getCoin().getPriceChange24h());
        dto.setPriceChangePercentage24h(trackedCurrency.getCoin().getPriceChangePercentage24h());
        dto.setMarketCapRank(trackedCurrency.getCoin().getMarketCapRank());

        // getting data from the linked Coin
        Coin coin = trackedCurrency.getCoin();
        dto.setCurrentPrice(coin.getCurrentPrice());
        dto.setPriceChange24h(coin.getPriceChange24h());
        dto.setPriceChangePercentage24h(coin.getPriceChangePercentage24h());
        dto.setMarketCapRank(coin.getMarketCapRank());

        dto.setMarketCap(coin.getMarketCap());
        dto.setTotalVolume(coin.getTotalVolume());
        return dto;
    }
}