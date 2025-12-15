package kosukeroku.token_radar.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AlertType {
    PRICE_ABOVE("price_above"),
    PRICE_BELOW("price_below"),
    PERCENTAGE_UP("percentage_up"),
    PERCENTAGE_DOWN("percentage_down");

    private final String value;

    AlertType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AlertType fromValue(String value) {
        for (AlertType type : AlertType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown alert type: " + value);
    }

    public boolean isPriceBased() {
        return this == PRICE_ABOVE || this == PRICE_BELOW;
    }

    public boolean isPercentageBased() {
        return this == PERCENTAGE_UP || this == PERCENTAGE_DOWN;
    }
}