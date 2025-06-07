package br.com.luarend.SmartTradeBot.trading.indicator;

import br.com.luarend.SmartTradeBot.domain.model.Candle;

import java.util.List;

public interface Indicator<T> {

    T calculate(List<Candle> candles);
}
