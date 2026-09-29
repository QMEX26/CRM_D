package com.crm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.calls.threshold")
@Data
public class CallTrackingProperties {

    /**
     * Threshold in seconds below which a connected call is classified as VERY_SHORT (default: 30)
     */
    private int veryShort = 30;

    /**
     * Threshold in seconds below which a connected call is classified as SHORT_CALL (default: 60)
     */
    private int shortThreshold = 60;
}
