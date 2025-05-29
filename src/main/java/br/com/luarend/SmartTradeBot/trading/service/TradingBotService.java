package br.com.luarend.SmartTradeBot.trading.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradingBotService {

    @Autowired
    private BinanceApiService binanceApiService;

    @Value("${bot.enabled:true}")
    private boolean botEnabled;

    @Scheduled(fixedDelayString = "${bot.trading.interval:30000}")
    public void executeTradingCycle() {
        if (!botEnabled) {
            log.debug("Bot está desabilitado");
            return;
        }

        try {
            log.info("Iniciando ciclo de trading...");

            String btcPrice = binanceApiService.getSymbolPrice("BTCUSDT");
            String klines = binanceApiService.getKlines("BTCUSDT", "1m", 100);

            log.info("Preço atual BTC: {}", btcPrice);
            log.debug("Klines: {}", klines);

        } catch (Exception e) {
            log.error("Erro durante ciclo de trading: {}", e.getMessage(), e);
        }
    }

    public void startBot() {
        botEnabled = true;
        log.info("Bot iniciado");
        executeTradingCycle();
    }

    public void stopBot() {
        botEnabled = false;
        log.info("Bot parado");
    }
}
