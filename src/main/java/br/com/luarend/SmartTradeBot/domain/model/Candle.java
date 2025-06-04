package br.com.luarend.SmartTradeBot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Candle {
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private long timestamp;
}
