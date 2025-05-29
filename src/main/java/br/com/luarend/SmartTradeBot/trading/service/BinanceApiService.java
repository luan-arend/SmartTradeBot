package br.com.luarend.SmartTradeBot.trading.service;

import br.com.luarend.SmartTradeBot.exchange.config.BinanceProperties;
import br.com.luarend.SmartTradeBot.exchange.service.TimestampSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

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
        try {
            long timestamp = timestampSyncService.getSynchronizedTimestamp();
            String queryString = "timestamp=" + timestamp;
            String signature = generateSignature(queryString, binanceProperties.getSecret());

            //String encodedSignature = URLEncoder.encode(signature, StandardCharsets.UTF_8);
            String url = binanceProperties.getBaseUrl() + "/v3/account?" + queryString + "&signature=" + signature;


            System.out.println("Query String: " + queryString);
            System.out.println("Signature: " + signature);
            System.out.println(binanceProperties.getKey());

            return webClient
                    .get()
                    .uri(url)
                    .header("X-MBX-APIKEY", binanceProperties.getKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            //log.error("Erro na requisição: {}", e.getMessage());
            return "Erro: 1 " + e.getMessage();
        }
    }

    private String generateSignature(String data, String secret) {
        try {
            System.out.println("Gerando assinatura HMAC com dados: " + data);
            System.out.println("Usando segredo: " + secret);
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar assinatura HMAC", e);
        }
    }
}
