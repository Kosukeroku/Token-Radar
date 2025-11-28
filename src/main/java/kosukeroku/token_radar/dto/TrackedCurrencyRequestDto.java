package kosukeroku.token_radar.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class TrackedCurrencyRequestDto {
    @NotNull
    private String coinId;

    private Double threshold;
}