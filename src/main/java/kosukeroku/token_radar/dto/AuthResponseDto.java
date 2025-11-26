package kosukeroku.token_radar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
}