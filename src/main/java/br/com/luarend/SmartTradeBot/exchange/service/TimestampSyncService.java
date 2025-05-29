package br.com.luarend.SmartTradeBot.exchange.service;

import br.com.luarend.SmartTradeBot.exchange.config.BinanceProperties;
import br.com.luarend.SmartTradeBot.exchange.dto.ServerTimeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimestampSyncService {

    private static final long DEFAULT_OFFSET = 0L;
    private static final String SERVER_TIME_ENDPOINT = "/v3/time";

    private final BinanceProperties binanceProperties;
    private final WebClient webClient;

    private final AtomicLong lastSyncTime = new AtomicLong(DEFAULT_OFFSET);
    private final AtomicLong timestampOffset = new AtomicLong(DEFAULT_OFFSET);

    /**
     * Sincroniza o timestamp com o servidor Binance se necessário
     *
     * @param force força a sincronização independente do intervalo
     */
    public void syncTimestamp(boolean force) {
        if (!binanceProperties.getTimestampSync().isEnabled()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (force || shouldSync(currentTime)) {
            performSync(currentTime);
        }
    }

    /**
     * Retorna timestamp sincronizado com o servidor Binance
     *
     * @return timestamp ajustado com offset do servidor
     */
    public long getSynchronizedTimestamp() {
        if (!binanceProperties.getTimestampSync().isEnabled()) {
            return System.currentTimeMillis();
        }

        syncTimestamp(false);
        return System.currentTimeMillis() + timestampOffset.get();
    }

    /**
     * Força uma nova sincronização imediata
     */
    public void forceResync() {
        syncTimestamp(true);
    }

    private boolean shouldSync(long currentTime) {
        return currentTime - lastSyncTime.get() >= binanceProperties.getTimestampSync().getIntervalMs();
    }

    private void performSync(long currentTime) {
        try {
            long serverTime = fetchServerTime();
            timestampOffset.set(serverTime - System.currentTimeMillis());
            lastSyncTime.set(currentTime);

            if (binanceProperties.getTimestampSync().isVerbose()) {
                log.info("⏰ Timestamp sincronizado - Offset: {}ms", timestampOffset.get());
            }
        } catch (Exception e) {
            log.error("⚠️ Erro ao sincronizar timestamp", e);
            timestampOffset.set(0L);
        }
    }

    private long fetchServerTime() {
        try {

            ServerTimeResponse response = webClient
                    .get()
                    .uri("/v3/time")
                    .retrieve()
                    .bodyToMono(ServerTimeResponse.class)
                    .block(Duration.ofSeconds(5));

            if (response == null) {
                throw new RuntimeException("Resposta nula do servidor");
            }

            return response.getServerTime();
        } catch (Exception e) {
            log.error("Falha crítica ao obter serverTime", e);
            throw new RuntimeException(e);
        }
    }
}
