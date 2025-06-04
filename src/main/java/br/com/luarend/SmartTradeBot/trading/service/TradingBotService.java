package br.com.luarend.SmartTradeBot.trading.service;

import br.com.luarend.SmartTradeBot.domain.model.Candle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

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
            List<Candle> klines = binanceApiService.getKlines("BTCUSDT", "5m", 100);

            log.info("Preço atual BTC: {}", btcPrice);
            log.info("Klines: {}", klines);

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
