package kosukeroku.token_radar.dto;

import kosukeroku.token_radar.model.enums.AlertType;
import kosukeroku.token_radar.model.enums.AlertStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertResponseDto {
    private Long id;
    private String coinId;
    private String coinName;
    private String coinSymbol;
    private String coinImageUrl;
    private AlertType type;
    private AlertStatus status;
    private BigDecimal thresholdValue;
    private BigDecimal currentPrice;
    private BigDecimal initialPrice;
    private LocalDateTime createdAt;
    private LocalDateTime triggeredAt;
    private BigDecimal triggeredPrice;
    private String notificationMessage;
    private Boolean isRead;
}