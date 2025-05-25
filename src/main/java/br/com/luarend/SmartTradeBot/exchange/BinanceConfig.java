package br.com.luarend.SmartTradeBot.exchange;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class BinanceConfig {
    @Value("${binance.api.base.url}")
    private String baseUrl;
    @Value("${binance.api.stream.url}")
    private String streamUrl;
    @Value("${binance.api.key}")
    private String key;
    @Value("${binance.api.secret}")
    private String secret;
    @Value("${binance.api.timeout}")
    private int timeout;
}
