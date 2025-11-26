package kosukeroku.token_radar.dto;

import kosukeroku.token_radar.model.TrackedCurrency;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private List<TrackedCurrency> trackedCurrencies;
}
