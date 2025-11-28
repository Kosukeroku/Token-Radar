package kosukeroku.token_radar.repository;


import kosukeroku.token_radar.model.TrackedCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackedCurrencyRepository extends JpaRepository<TrackedCurrency, Long> {
    List<TrackedCurrency> findByUserId(Long userId);
    boolean existsByUserIdAndCoinId(Long userId, String coinId);
    Optional<TrackedCurrency> findByUserIdAndCoinId(Long userId, String coinId);
    void deleteByUserIdAndCoinId(Long userId, String coinId);

}
