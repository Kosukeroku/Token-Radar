package kosukeroku.token_radar.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AlertStatus {
    ACTIVE("active"),
    TRIGGERED("triggered"),
    READ("read");

    private final String value;

    AlertStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}