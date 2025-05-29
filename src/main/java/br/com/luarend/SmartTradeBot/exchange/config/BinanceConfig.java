package br.com.luarend.SmartTradeBot.exchange.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(BinanceProperties.class)
@RequiredArgsConstructor
public class BinanceConfig {

    private final BinanceProperties binanceProperties;

    @Bean
    public WebClient binanceWebClient() {
        return WebClient.builder()
                .baseUrl(binanceProperties.getBaseUrl())
                .build();
    }
}
