package xyz.harbor.calendly_based_take_home.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SessionLength {
    @JsonProperty("15m")
    MINUTES_15("15 Minutes", 15 * 60),
    @JsonProperty("30m")
    MINUTES_30("30 Minutes", 30 * 60),
    @JsonProperty("45m")
    MINUTES_45("45 Minutes", 45 * 60),
    @JsonProperty("60m")
    MINUTES_60("1 Hour", 60 * 60);

    public final String label;
    public final int timeInSeconds;

    SessionLength(String label, int timeInSeconds){
        this.label = label;
        this.timeInSeconds = timeInSeconds;
    }

    public static SessionLength fromJsonString(String jsonLabel){
        return switch (jsonLabel) {
            case "15m" -> MINUTES_15;
            case "30m" -> MINUTES_30;
            case "45m" -> MINUTES_45;
            case "60m" -> MINUTES_60;
            default -> throw new IllegalArgumentException("Unknown session length: " + jsonLabel);
        };
    }

    public static SessionLength fromLabel(String sessionLabel){
        for (SessionLength session : SessionLength.values()){
            if(session.label.equalsIgnoreCase(sessionLabel)){
                return session;
            }
        }
        throw new IllegalArgumentException(sessionLabel + " as label does not exist for any tag.");
    }
}
