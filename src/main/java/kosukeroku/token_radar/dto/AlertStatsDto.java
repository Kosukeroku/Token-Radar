package kosukeroku.token_radar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertStatsDto {
    private Long totalAlerts;
    private Long activeAlerts;
    private Long triggeredAlerts;
    private Long readAlerts;
    private Long unreadCount;
}