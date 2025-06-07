package br.com.luarend.SmartTradeBot.trading.strategy;

import br.com.luarend.SmartTradeBot.domain.model.Candle;
import br.com.luarend.SmartTradeBot.trading.indicator.Indicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("MovingAverageCrossover")
public class MovingAverageCrossoverStrategy implements TradingStrategy {

    private final Indicator<List<Double>> shortSmaIndicator;
    private final Indicator<List<Double>> longSmaIndicator;

    public MovingAverageCrossoverStrategy(
            @Qualifier("shortSma") Indicator<List<Double>> shortSmaIndicator,
            @Qualifier("longSma") Indicator<List<Double>> longSmaIndicator) {
        this.shortSmaIndicator = shortSmaIndicator;
        this.longSmaIndicator = longSmaIndicator;
    }

    @Override
    public TradeSignal decide(List<Candle> klines) {
        // 1. Calcular os indicadores usando os componentes injetados
        List<Double> shortSma = shortSmaIndicator.calculate(klines);
        List<Double> longSma = longSmaIndicator.calculate(klines);

        if (shortSma.isEmpty() || longSma.isEmpty()) {
            log.warn("Dados de mercado insuficientes para a estratégia '{}'.", getStrategyName());
            return TradeSignal.HOLD;
        }

        // 2. Lógica de cruzamento
        int lastCompleteCandleIndex = klines.size() - 2;
        int previousCandleIndex = lastCompleteCandleIndex - 1;

        if (previousCandleIndex < 0) return TradeSignal.HOLD;

        double lastShortValue = shortSma.get(lastCompleteCandleIndex);
        double lastLongValue = longSma.get(lastCompleteCandleIndex);
        double prevShortValue = shortSma.get(previousCandleIndex);
        double prevLongValue = longSma.get(previousCandleIndex);

        boolean isBuySignal = prevShortValue <= prevLongValue && lastShortValue > lastLongValue;
        if (isBuySignal) {
            log.info("SINAL DE COMPRA DETECTADO PELA ESTRATÉGIA {}", getStrategyName());
            return TradeSignal.BUY;
        }

        boolean isSellSignal = prevShortValue >= prevLongValue && lastShortValue < lastLongValue;
        if (isSellSignal) {
            log.info("SINAL DE VENDA DETECTADO PELA ESTRATÉGIA {}", getStrategyName());
            return TradeSignal.SELL;
        }

        return TradeSignal.HOLD;
    }

    @Override
    public String getStrategyName() {
        return "MovingAverageCrossover";
    }
}
