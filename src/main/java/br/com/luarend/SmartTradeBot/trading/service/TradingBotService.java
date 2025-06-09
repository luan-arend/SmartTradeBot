package br.com.luarend.SmartTradeBot.trading.service;

import br.com.luarend.SmartTradeBot.domain.model.Candle;
import br.com.luarend.SmartTradeBot.trading.strategy.TradeSignal;
import br.com.luarend.SmartTradeBot.trading.strategy.TradingStrategy;
import br.com.luarend.SmartTradeBot.util.TelegramNotification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TradingBotService {

    @Value("${bot.enabled:true}")
    private boolean botEnabled;

    @Autowired
    private BinanceApiService binanceApiService;

    @Autowired
    private Map<String, TradingStrategy> strategies;

    @Autowired
    private TelegramNotification notification;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void executeTradingCycle() {
        if (!botEnabled) {
            log.debug("Bot está desabilitado");
            return;
        }

        try {
            log.info("Iniciando ciclo de trading...");

            List<Candle> klines = binanceApiService.getKlines("BTCUSDT", "1m", 100);

            log.info("Dados de mercado obtidos: {} candles", klines.size());

            List<TradeSignal> signals = strategies.values().stream()
                    .map(strategy -> {
                        TradeSignal signal = strategy.decide(klines);
                        log.info("Estratégia '{}' sinalizou: {}", strategy.getStrategyName(), signal);
                        return signal;
                    })
                    .collect(Collectors.toList());

            TradeSignal finalDecide = consolidateSignals(signals);

            log.info("Decisão final: {}", finalDecide);

            if (finalDecide == TradeSignal.BUY) {
                log.info("Executando ordem de COMPRA com base no consenso das estratégias.");
                notification.sendAlert("Executando ordem de COMPRA com base no consenso das estratégias.");
            } else if (finalDecide == TradeSignal.SELL) {
                log.info("Executando ordem de VENDA com base no consenso das estratégias.");
                notification.sendAlert("Executando ordem de VENDA com base no consenso das estratégias.");
            }

        } catch (Exception e) {
            log.error("Erro durante ciclo de trading: {}", e.getMessage(), e);
        }
    }

    private TradeSignal consolidateSignals(List<TradeSignal> signals) {
        long buySignals = signals.stream().filter(s -> s == TradeSignal.BUY).count();
        long sellSignals = signals.stream().filter(s -> s == TradeSignal.SELL).count();

        if (buySignals > 0 && sellSignals > 0) {
            log.warn("Sinais conflitantes encontrados (COMPRA x VENDA). Nenhuma ação será tomada.");
            return TradeSignal.HOLD;
        }

        if (buySignals > 0) {
            log.info("Consenso para COMPRA validado por {} de {} estratégias.", buySignals, strategies.size());
            return TradeSignal.BUY;
        }

        if (sellSignals > 0) {
            log.info("Consenso para VENDA validado por {} de {} estratégias.", sellSignals, strategies.size());
            return TradeSignal.SELL;
        }

        return TradeSignal.HOLD;
    }

    public void startBot() {
        botEnabled = true;
        log.info("Bot iniciado");
        executor.submit(() -> {
            while (botEnabled) {
                executeTradingCycle();
                try {
                    long intervalInMillis = 30000;
                    TimeUnit.MILLISECONDS.sleep(intervalInMillis);
                } catch (InterruptedException e) {
                    log.error("Loop do bot foi interrompido.");
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Loop manual do bot foi finalizado.");
        });
    }

    public void stopBot() {
        botEnabled = false;
        log.info("Bot parado");
    }
}
