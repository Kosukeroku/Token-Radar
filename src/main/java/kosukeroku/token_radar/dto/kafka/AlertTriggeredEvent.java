package kosukeroku.token_radar.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertTriggeredEvent {
    private Long alertId;
    private Long userId;
    private String username;
    private String coinId;
    private String coinName;
    private String coinSymbol;
    private String alertType;
    private BigDecimal thresholdValue;
    private BigDecimal triggeredPrice;
    private String notificationMessage;
    private LocalDateTime triggeredAt;
}