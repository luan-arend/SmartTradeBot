package br.com.luarend.SmartTradeBot.trading.service;

import br.com.luarend.SmartTradeBot.exchange.config.BinanceProperties;
import br.com.luarend.SmartTradeBot.exchange.service.TimestampSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class BinanceApiService {

    @Autowired
    private BinanceProperties binanceProperties;

    @Autowired
    private TimestampSyncService timestampSyncService;

    @Autowired
    private WebClient webClient;

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
        try {
            long timestamp = timestampSyncService.getSynchronizedTimestamp();
            String queryString = "timestamp=" + timestamp;
            String url = "/v3/account?" + queryString + "&signature=" + generateSignature(queryString, binanceProperties.getSecret());

            return webClient
                    .get()
                    .uri(url)
                    .header("X-MBX-APIKEY", binanceProperties.getKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Erro na requisição: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String generateSignature(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Erro crítico ao gerar assinatura HMAC: Algoritmo ou chave inválida.", e);
            throw new RuntimeException("Erro ao gerar assinatura HMAC: Configuração de criptografia inválida.", e);
        } catch (Exception e) {
            log.error("Erro inesperado ao gerar assinatura HMAC para dados: '{}'", data, e);
            throw new RuntimeException("Erro inesperado ao gerar assinatura HMAC.", e);
        }
    }
}
