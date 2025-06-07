package br.com.luarend.SmartTradeBot.trading.strategy;

/**
 * Representa o sinal de negociação gerado por uma estratégia.
 * BUY: Sinal para executar uma ordem de compra.
 * SELL: Sinal para executar uma ordem de venda.
 * HOLD: Sinal para não fazer nada e aguardar a próxima avaliação.
 */
public enum TradeSignal {
    BUY,
    SELL,
    HOLD
}
