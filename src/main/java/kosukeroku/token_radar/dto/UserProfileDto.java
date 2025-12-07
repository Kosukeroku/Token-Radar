package kosukeroku.token_radar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private List<TrackedCurrencyResponseDto> trackedCurrencies;
    private Long trackedCount;
}
