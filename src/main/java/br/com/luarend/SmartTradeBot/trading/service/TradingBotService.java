package br.com.luarend.SmartTradeBot.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TradingBotService {

    private static final Logger logger = LoggerFactory.getLogger(TradingBotService.class);

    @Autowired
    private BinanceApiService binanceApiService;

    @Value("${bot.enabled:true}")
    private boolean botEnabled;

    @Scheduled(fixedDelayString = "${bot.trading.interval:30000}")
    public void executeTradingCycle() {
        if (!botEnabled) {
            logger.debug("Bot está desabilitado");
            return;
        }

        try {
            logger.info("Iniciando ciclo de trading...");

            // 1. Obter dados de mercado
            String btcPrice = binanceApiService.getSymbolPrice("BTCUSDT");
            String klines = binanceApiService.getKlines("BTCUSDT", "1m", 100);

            logger.info("Preço atual BTC: {}", btcPrice);

            // 2. Analisar dados
            //boolean shouldBuy = analysisService.analyzeMarket("BTCUSDT", klines);

            // 3. Executar estratégia
            /*if (shouldBuy) {
                logger.info("Sinal de compra detectado para BTCUSDT");
                // Aqui você implementaria a lógica de compra
            }*/

        } catch (Exception e) {
            logger.error("Erro durante ciclo de trading: {}", e.getMessage(), e);
        }
    }

    public void startBot() {
        botEnabled = true;
        logger.info("Bot iniciado");
        binanceApiService.getAccountInfo();

    }

    public void stopBot() {
        botEnabled = false;
        logger.info("Bot parado");
    }
}
