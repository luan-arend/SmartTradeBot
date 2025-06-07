package br.com.luarend.SmartTradeBot.trading.strategy;

import br.com.luarend.SmartTradeBot.domain.model.Candle;

import java.util.List;

public interface TradingStrategy {
    TradeSignal decide(List<Candle> klines);

    String getStrategyName();
}
