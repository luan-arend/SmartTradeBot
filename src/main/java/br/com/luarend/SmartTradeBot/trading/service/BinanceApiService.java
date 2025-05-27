package br.com.luarend.SmartTradeBot.trading.service;

import br.com.luarend.SmartTradeBot.exchange.config.BinanceProperties;
import br.com.luarend.SmartTradeBot.exchange.service.TimestampSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class BinanceApiService {

    private final BinanceProperties binanceProperties;
    private final TimestampSyncService timestampSyncService;
    private final WebClient webClient;

    public String getSymbolPrice(String symbol) {
        return webClient
                .get()
                .uri("/v3/ticker/price?symbol={symbol}", symbol)
                .header("X-MBX-APIKEY", binanceProperties.getKey())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getKlines(String symbol, String interval, int limit) {
        return webClient
                .get()
                .uri("/v3/klines?symbol={symbol}&interval={interval}&limit={limit}",
                        symbol, interval, limit)
                .header("X-MBX-APIKEY", binanceProperties.getKey())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getAccountInfo() {
        long timestamp = timestampSyncService.getSynchronizedTimestamp();
        String queryString = "timestamp=" + timestamp;
        String signature = generateSignature(queryString);

        return webClient
                .get()
                .uri("/v3/account?{queryString}&signature={signature}",
                        queryString, signature)
                .header("X-MBX-APIKEY", binanceProperties.getKey())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String generateSignature(String data) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    binanceProperties.getSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            sha256Hmac.init(secretKey);

            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar assinatura HMAC", e);
        }
    }
}
