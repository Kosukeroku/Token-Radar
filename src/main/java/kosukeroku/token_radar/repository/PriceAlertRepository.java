package kosukeroku.token_radar.repository;

import kosukeroku.token_radar.model.PriceAlert;
import kosukeroku.token_radar.model.enums.AlertStatus;
import kosukeroku.token_radar.model.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {


    List<PriceAlert> findByUserIdAndStatus(Long userId, AlertStatus status);

    List<PriceAlert> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, AlertStatus status);

    List<PriceAlert> findByCoinIdAndStatus(String coinId, AlertStatus status);

    @Query("SELECT pa FROM PriceAlert pa WHERE pa.coin.id = :coinId AND pa.status = 'ACTIVE'")
    List<PriceAlert> findActiveAlertsForCoin(@Param("coinId") String coinId);

    Optional<PriceAlert> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndCoinIdAndTypeAndStatus(
            Long userId, String coinId, kosukeroku.token_radar.model.enums.AlertType type, AlertStatus status);

    @Query("SELECT COUNT(pa) FROM PriceAlert pa WHERE pa.user.id = :userId AND pa.status = 'TRIGGERED'")
    long countTriggeredAlerts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PriceAlert pa SET pa.status = 'READ' WHERE pa.user.id = :userId AND pa.status = 'TRIGGERED'")
    int markAllTriggeredAsRead(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PriceAlert pa WHERE pa.user.id = :userId AND pa.status = 'READ'")
    int deleteReadAlerts(@Param("userId") Long userId);

    Optional<PriceAlert> findByUserIdAndCoinIdAndTypeAndStatus(Long userId, String coinId, AlertType type, AlertStatus status);

    @Query("SELECT pa FROM PriceAlert pa JOIN FETCH pa.coin c WHERE pa.user.id = :userId")
    List<PriceAlert> findByUserId(@Param("userId") Long userId);

    @Query("SELECT pa FROM PriceAlert pa WHERE pa.user.id = :userId AND pa.status IN (:statuses) ORDER BY COALESCE(pa.triggeredAt, pa.createdAt) DESC")
    List<PriceAlert> findNotificationsForUser(@Param("userId") Long userId, @Param("statuses") List<AlertStatus> statuses);

    List<PriceAlert> findByUserIdAndCoinId(Long userId, String coinId);
}