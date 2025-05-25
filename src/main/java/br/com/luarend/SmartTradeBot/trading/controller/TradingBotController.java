package br.com.luarend.SmartTradeBot.trading.controller;

import br.com.luarend.SmartTradeBot.trading.service.BinanceApiService;
import br.com.luarend.SmartTradeBot.trading.service.TradingBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot")
public class TradingBotController {

    @Autowired
    private TradingBotService tradingBotService;

    @Autowired
    private BinanceApiService binanceApiService;

    @PostMapping("/start")
    public ResponseEntity<String> startBot() {
        tradingBotService.startBot();
        return ResponseEntity.ok("Bot iniciado com sucesso");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopBot() {
        tradingBotService.stopBot();
        return ResponseEntity.ok("Bot parado com sucesso");
    }

    @GetMapping("/price/{symbol}")
    public ResponseEntity<String> getPrice(@PathVariable String symbol) {
        String price = binanceApiService.getSymbolPrice(symbol);
        return ResponseEntity.ok(price);
    }

    @GetMapping("/account")
    public ResponseEntity<String> getAccountInfo() {
        String accountInfo = binanceApiService.getAccountInfo();
        return ResponseEntity.ok(accountInfo);
    }
}
