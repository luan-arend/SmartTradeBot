package br.com.luarend.SmartTradeBot.exchange.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "binance.api")
public class BinanceProperties {

    private String baseUrl;
    private String streamUrl;
    private String key;
    private String secret;
    private int timeout;
    private TimestampSync timestampSync = new TimestampSync();

    @Data
    public static class TimestampSync {
        private boolean enabled;
        private boolean verbose;
        private long intervalMs;
        private long maxOffsetMs;
    }
}
