package io.github.unknownredgreen;

import io.github.unknownredgreen.gui.DisplayMessagesWindow;
import io.github.unknownredgreen.gui.MainWindow;
import io.github.unknownredgreen.gui.SendMessagesWindow;
import io.github.unknownredgreen.util.Strings;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    private final Map<Long, Chat> activeChats = new HashMap<>();
    private final Map<Long, List<String>> messages = new HashMap<>();
    @Setter
    private MainWindow mainWindow;
    @Setter
    private DisplayMessagesWindow displayMessagesWindow;
    @Setter
    private SendMessagesWindow sendMessagesWindow;

    public Map<Long, Chat> getActiveChats() {
        return Collections.unmodifiableMap(activeChats);
    }
    public String[] getFormattedMessages(long chatId) {
        List<String> allMessages = messages.get(chatId);
        if (allMessages == null) return null;
        return allMessages.toArray(new String[0]);
    }

    /**
     * @return null if message was sent, string what went wrong if failed
     */
    public String sendMessage(String text, long chatId, Integer messageIdToReply) {
        try {
            if (messageIdToReply != null) {
                Message msg = execute(SendMessage.builder()
                        .text(text)
                        .chatId(String.valueOf(chatId))
                        .replyToMessageId(messageIdToReply)
                        .build()
                );
                workWithMessage(msg);
            } else {
                Message msg = execute(SendMessage.builder()
                        .text(text)
                        .chatId(String.valueOf(chatId))
                        .build()
                );
                workWithMessage(msg);
            }
            return null;
        } catch (TelegramApiException e) {
            return e.getMessage();
        }
    }

    /**
     * @return null if message was sent, string what went wrong if failed
     */
    public String sendMessage(String text, long chatId) {
        return sendMessage(text, chatId, null);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;
        Message msg = update.getMessage();
        Chat chat = msg.getChat();

        workWithChat(chat);
        workWithMessage(msg);
    }


    private void workWithChat(Chat chat) {
        long chatId = chat.getId();
        boolean chatAlreadyExists = activeChats.containsKey(chatId);

        if (!chatAlreadyExists) {
            mainWindow.newChatDetected(chat);
        }

        activeChats.put(chatId, chat);
    }


    private void workWithMessage(Message msg) {
        long chatId = msg.getChatId();
        String formattedMessageStart = "";

        if (msg.isReply()) {
            Message repliedMessage = msg.getReplyToMessage();

            String repliedMessageText;
            repliedMessageText = repliedMessage.getText();
            if (repliedMessageText == null) repliedMessageText = repliedMessage.getCaption();

            String cutText = Strings.cutString(repliedMessageText, 40);

            if (cutText == null) cutText = getAttachedMediaType(repliedMessage);
            else cutText = '\'' + cutText + '\'';

            formattedMessageStart += "replying to " +
                    repliedMessage.getMessageId() +
                    ' ' + "(%s)".formatted(cutText) + '\n';
        }

        StringBuilder msgText = new StringBuilder();
        String attachedMedia = getAttachedMediaType(msg);
        if (attachedMedia != null) msgText.append('-').append(attachedMedia).append('-').append('\n');

        String caption = msg.getCaption();
        if (caption != null) msgText.append(caption);
        if (msg.hasText()) msgText.append(msg.getText());

        User user = msg.getFrom();
        String username = user.getUserName();
        String identifier;
        if (username != null) {
            identifier = "UN{@%s}".formatted(username);
        } else {
            identifier = "UID[%d]".formatted(user.getId());
        }
        String displayName = "";

        String firstName = user.getFirstName();
        String lastName = user.getLastName();

        displayName += firstName;
        if (lastName != null) displayName += ' ' + lastName;

        String formattedMessage = formattedMessageStart + """
                message id: %d
                |%s| %s
                (%s):
                %s
                """
                .formatted(
                        msg.getMessageId(),
                        Instant.ofEpochSecond(msg.getDate())
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                        identifier,
                        displayName,
                        msgText.toString()
                );

        messages.computeIfAbsent(chatId, k -> new ArrayList<>()).add(formattedMessage);
        if (displayMessagesWindow != null) displayMessagesWindow.newMessageReceived(chatId, formattedMessage);
    }

    private String getAttachedMediaType(Message msg) {
        if (msg == null) return null;

        if (msg.hasPhoto()) return "photo";
        if (msg.hasVideo()) return "video";
        if (msg.hasSticker()) return "sticker";
        if (msg.hasVoice()) return "voice message";
        if (msg.hasVideoNote()) return "video note";
        if (msg.hasAnimation()) return "GIF";
        if (msg.hasPoll()) return "poll";
        if (msg.hasAudio()) return "audio";
        if (msg.hasDocument()) return "file";

        return null;
    }
}