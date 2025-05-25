package br.com.luarend.SmartTradeBot.trading.service;

import br.com.luarend.SmartTradeBot.exchange.BinanceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class BinanceApiService {

    @Autowired
    private BinanceConfig binanceConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getSymbolPrice(String symbol) {
        String url = binanceConfig.getBaseUrl() + "/v3/ticker/price?symbol=" + symbol;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-MBX-APIKEY", binanceConfig.getKey());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    public String getKlines(String symbol, String interval, int limit) {
        String url = String.format("%s/v3/klines?symbol=%s&interval=%s&limit=%d",
                binanceConfig.getBaseUrl(), symbol, interval, limit);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-MBX-APIKEY", binanceConfig.getKey());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    public String getAccountInfo() {
        long timestamp = Instant.now().toEpochMilli();
        String queryString = "timestamp=" + timestamp;
        String signature = generateSignature(queryString);

        System.out.printf("Signature: %s%n", signature);
        System.out.printf("timestamp: %s%n", timestamp);
        System.out.printf(binanceConfig.getBaseUrl());

        String url = binanceConfig.getBaseUrl() + "/v3/account?" + queryString + "&signature=" + signature;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-MBX-APIKEY", binanceConfig.getKey());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    private String generateSignature(String data) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(binanceConfig.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
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
