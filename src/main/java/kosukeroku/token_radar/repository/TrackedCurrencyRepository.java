package kosukeroku.token_radar.repository;


import kosukeroku.token_radar.model.TrackedCurrency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackedCurrencyRepository extends JpaRepository<TrackedCurrency, Long> {

    List<TrackedCurrency> findByUserId(Long userId);

    Page<TrackedCurrency> findByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndCoinId(Long userId, String coinId);

    Optional<TrackedCurrency> findByUserIdAndCoinId(Long userId, String coinId);

    @Query("SELECT tc FROM TrackedCurrency tc WHERE tc.user.id = :userId ORDER BY tc.coin.marketCapRank ASC")
    List<TrackedCurrency> findByUserIdOrderByCoinMarketCapRankAsc(Long userId);

    void deleteByUserIdAndCoinId(Long userId, String coinId);

    // for counter
    Long countByUserId(Long userId);

}
