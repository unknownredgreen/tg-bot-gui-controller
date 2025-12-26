package io.github.unknownredgreen;

import io.github.unknownredgreen.gui.BasicWindow;
import io.github.unknownredgreen.gui.MainWindow;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.awt.*;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        int neededArgCount = 2;
        String requiredArgsAdvice = """
                Required:
                1: bot username
                2: bot token
                """;

        if (args.length < neededArgCount) {
            throw new RuntimeException("""
                    Too few args
                    %s
                    """.formatted(requiredArgsAdvice));
        } else if (args.length > neededArgCount) {
            throw new RuntimeException("""
                    Too many args
                    %s
                    """.formatted(requiredArgsAdvice));
        }

        Storage storage = new Storage();
        BasicWindow.setStorage(storage);

        String botUsername = args[0];
        String botToken = args[1];

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        Bot bot = new Bot(botUsername, botToken);
        BasicWindow.setBot(bot);
        botsApi.registerBot(bot);
        MainWindow mainWindow = new MainWindow();
    }
}