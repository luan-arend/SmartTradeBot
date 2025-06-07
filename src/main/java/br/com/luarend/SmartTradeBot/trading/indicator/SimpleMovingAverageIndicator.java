package br.com.luarend.SmartTradeBot.trading.indicator;

import br.com.luarend.SmartTradeBot.domain.model.Candle;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementação do indicador de Média Móvel Simples (SMA).
 * Esta classe é configurada com um período e calcula a SMA para esse período.
 * Não é um @Component, pois será instanciada como um @Bean na configuração.
 */
@RequiredArgsConstructor
public class SimpleMovingAverageIndicator implements Indicator<List<Double>> {

    private final int period;

    @Override
    public List<Double> calculate(List<Candle> candles) {
        if (candles == null || candles.size() < this.period) {
            return Collections.emptyList();
        }

        List<Double> smaValues = new ArrayList<>();
        for (int i = 0; i < candles.size(); i++) {
            if (i < this.period - 1) {
                smaValues.add(0.0);
            } else {
                double sum = 0.0;
                for (int j = 0; j < this.period; j++) {
                    sum += candles.get(i - j).getClose();
                }
                smaValues.add(sum / this.period);
            }
        }

        return smaValues;
    }
}
