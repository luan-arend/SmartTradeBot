package br.com.luarend.SmartTradeBot.trading.strategy;

import br.com.luarend.SmartTradeBot.domain.model.Candle;
import br.com.luarend.SmartTradeBot.trading.indicator.Indicator;
import br.com.luarend.SmartTradeBot.trading.indicator.SimpleMovingAverageIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("MovingAverageCrossover")
public class MovingAverageCrossoverStrategy implements TradingStrategy {

    private final Indicator<List<Double>> shortSmaIndicator;
    private final Indicator<List<Double>> longSmaIndicator;

    public MovingAverageCrossoverStrategy() {
        this.shortSmaIndicator = new SimpleMovingAverageIndicator(7);
        this.longSmaIndicator = new SimpleMovingAverageIndicator(40);
    }

    @Override
    public TradeSignal decide(List<Candle> klines) {
        List<Double> shortSma = shortSmaIndicator.calculate(klines);
        List<Double> longSma = longSmaIndicator.calculate(klines);

        if (shortSma.isEmpty() || longSma.isEmpty() || shortSma.size() != longSma.size()) {
            log.warn("Dados de mercado insuficientes para a estratégia '{}'.", getStrategyName());
            return TradeSignal.HOLD;
        }

        int lastIndex = shortSma.size() - 1;
        double lastShortValue = shortSma.get(lastIndex);
        double lastLongValue = longSma.get(lastIndex);

        if (lastShortValue == 0.0 || lastLongValue == 0.0) {
            log.debug("Aguardando aquecimento dos dados para a média móvel.");
            return TradeSignal.HOLD;
        }

        log.info("Estratégia [{}]: Última Média Rápida: {} | Última Média Lenta: {}", getStrategyName(), lastShortValue, lastLongValue);

        if (lastShortValue > lastLongValue) {
            log.info("SINAL DE COMPRA: Média rápida está ACIMA da lenta.");
            return TradeSignal.BUY;
        } else {
            log.info("SINAL DE VENDA: Média rápida está ABAIXO ou IGUAL à lenta.");
            return TradeSignal.SELL;
        }
    }

    @Override
    public String getStrategyName() {
        return "MovingAverageCrossover";
    }
}
