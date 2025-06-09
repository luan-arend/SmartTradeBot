package br.com.luarend.SmartTradeBot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramNotification extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String username;

    @Value("${telegram.chat.id}")
    private String chatId;

    public TelegramNotification(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    public void sendAlert(String message) {
        if (chatId == null || chatId.isEmpty()) {
            System.out.println("ChatId não configurado!");
            return;
        }

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(message);

        try {
            System.out.println("Enviando alerta para o Telegram:" + message);
            execute(msg);
        } catch (TelegramApiException e) {
            System.out.println("Erro ao enviar alerta: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage response = new SendMessage();
            response.setChatId(update.getMessage().getChatId().toString());
            response.setText("Você disse: " + update.getMessage().getText());

            try {
                execute(response);
            } catch (TelegramApiException e) {
                System.out.println("Erro ao responder mensagem: " + e.getMessage());
            }
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return super.getBotToken();
    }
}
