package br.com.luarend.SmartTradeBot.trading.service;

import br.com.luarend.SmartTradeBot.domain.model.Candle;
import br.com.luarend.SmartTradeBot.trading.strategy.TradeSignal;
import br.com.luarend.SmartTradeBot.trading.strategy.TradingStrategy;
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

@Slf4j
@Service
public class TradingBotService {

    @Autowired
    private BinanceApiService binanceApiService;

    @Value("${bot.enabled:true}")
    private boolean botEnabled;

    @Autowired
    private Map<String, TradingStrategy> strategies;

    @Value("${bot.strategy.active}")
    private String activeStrategyName;

    private TradingStrategy activeStrategy;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void init() {
        this.activeStrategy = strategies.get(activeStrategyName);
        log.info("Estratégia ativa: {}", activeStrategy.getStrategyName());
    }

    //@Scheduled(fixedDelayString = "${bot.trading.interval:30000}")
    public void executeTradingCycle() {
        if (!botEnabled) {
            log.debug("Bot está desabilitado");
            return;
        }

        try {
            log.info("Iniciando ciclo de trading...");

            List<Candle> klines = binanceApiService.getKlines("BTCUSDT", "5m", 100);

            log.info("Dados de mercado obtidos: {} candles", klines.size());

            TradeSignal signal = activeStrategy.decide(klines);

            log.info("Sinal da estratégia {}: {}", activeStrategy.getStrategyName(), signal);

            if (signal == TradeSignal.BUY) {
                log.info("Executando ordem de COMPRA.");
            } else if (signal == TradeSignal.SELL) {
                log.info("Executando ordem de VENDA.");
            }

        } catch (Exception e) {
            log.error("Erro durante ciclo de trading: {}", e.getMessage(), e);
        }
    }

    public void startBot() {
        botEnabled = true;
        log.info("Bot iniciado");
        executor.submit(() -> {
            while (botEnabled) {
                executeTradingCycle();
                try {
                    // Pausa o loop pelo intervalo definido
                    long intervalInMillis = 30000; // Pode ser lido das properties
                    TimeUnit.MILLISECONDS.sleep(intervalInMillis);
                } catch (InterruptedException e) {
                    log.error("Loop do bot foi interrompido.");
                    Thread.currentThread().interrupt(); // Restaura o status de interrupção
                }
            }
            log.info("Loop manual do bot foi finalizado.");
        });

        //executeTradingCycle();
    }

    public void stopBot() {
        botEnabled = false;
        log.info("Bot parado");
    }
}
