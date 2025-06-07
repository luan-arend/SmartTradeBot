package br.com.luarend.SmartTradeBot.trading.config;

import br.com.luarend.SmartTradeBot.trading.indicator.Indicator;
import br.com.luarend.SmartTradeBot.trading.indicator.SimpleMovingAverageIndicator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TradingConfig {

    private static final int SHORT_PERIOD = 9;
    private static final int LONG_PERIOD = 21;

    @Bean
    @Qualifier("shortSma")
    public Indicator<List<Double>> shortSma() {
        return new SimpleMovingAverageIndicator(SHORT_PERIOD);
    }

    @Bean
    @Qualifier("longSma")
    public Indicator<List<Double>> longSma() {
        return new SimpleMovingAverageIndicator(LONG_PERIOD);
    }
}
