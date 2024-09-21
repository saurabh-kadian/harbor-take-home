package xyz.harbor.calendly_based_take_home.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SessionLength {
    @JsonProperty("1m")
    BLOCKING_MINUTE_1("1 Minute", 60),
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

    public static SessionLength fromLabel(String sessionLabel){
        for (SessionLength session : SessionLength.values()){
            if(session.label.equalsIgnoreCase(sessionLabel)){
                return session;
            }
        }
        throw new IllegalArgumentException(sessionLabel + " as label does not exist for any tag.");
    }
}
