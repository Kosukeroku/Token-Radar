package kosukeroku.token_radar.dto;

import jakarta.validation.constraints.NotNull;
import kosukeroku.token_radar.model.enums.AlertType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceAlertRequestDto {

    @NotNull(message = "Coin ID is required")
    private String coinId;

    @NotNull(message = "Alert type is required")
    private AlertType type;

    @NotNull(message = "Threshold value is required")
    private BigDecimal thresholdValue;

}