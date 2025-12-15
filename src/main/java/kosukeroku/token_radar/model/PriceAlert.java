package kosukeroku.token_radar.model;

import jakarta.persistence.*;
import kosukeroku.token_radar.model.enums.AlertType;
import kosukeroku.token_radar.model.enums.AlertStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_alerts", indexes = {
        @Index(name = "idx_alert_user_coin", columnList = "user_id,coin_id"),
        @Index(name = "idx_alert_status", columnList = "status"),
        @Index(name = "idx_alert_user_status", columnList = "user_id,status")
})
@Data
@NoArgsConstructor
public class PriceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(precision = 30, scale = 12, nullable = false)
    private BigDecimal thresholdValue;

    @Column(precision = 30, scale = 12)
    private BigDecimal initialPrice;

    @Column(precision = 30, scale = 12)
    private BigDecimal lastCheckedPrice;

    private LocalDateTime triggeredAt;

    @Column(precision = 30, scale = 12)
    private BigDecimal triggeredPrice;

    private String notificationMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public PriceAlert(User user, Coin coin, AlertType type, BigDecimal thresholdValue) {
        this.user = user;
        this.coin = coin;
        this.type = type;
        this.thresholdValue = thresholdValue;
        this.initialPrice = coin.getCurrentPrice();
        this.status = AlertStatus.ACTIVE;
    }
}