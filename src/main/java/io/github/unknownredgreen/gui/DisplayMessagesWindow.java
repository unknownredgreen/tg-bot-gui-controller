package io.github.unknownredgreen.gui;

import io.github.unknownredgreen.interfaces.HasScrollPane;
import io.github.unknownredgreen.util.Strings;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

@Slf4j
public final class DisplayMessagesWindow extends BasicWindow implements HasScrollPane {
    private final long chatId;
    private final String chatTitle;
    private JTextArea textArea;
    private JScrollBar scrollBar;
    private DefaultCaret textAreaCaret;

    public DisplayMessagesWindow(long chatId, String chatTitle) {
        this.chatId = chatId;
        this.chatTitle = chatTitle;
        bot.setDisplayMessagesWindow(this);
    }

    @Override
    protected void onInitialization() {
        JTextArea titleArea = new JTextArea();
        titleArea.setEditable(false);

        add(titleArea, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(true);
        textAreaCaret = (DefaultCaret) textArea.getCaret();
        textAreaCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollBar = scrollPane.getVerticalScrollBar();

        add(scrollPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            String[] existingMessages = bot.getFormattedMessages(chatId);
            if (existingMessages != null && existingMessages.length > 0) textArea.setText(String.join("\n", existingMessages));

            String cutChatTitle = Strings.cutString(chatTitle, 40);
            titleArea.setText("%s\n(ID:%d)".formatted(cutChatTitle, chatId));

            loadScrollState(scrollPane, chatId, storage);
        });
    }

    @Override
    protected void onWindowClosing() {
        saveScrollState(scrollBar, chatId, storage);
    }

    public void newMessageReceived(long chatId, String message) {
        if (chatId != this.chatId) return;
        boolean atBottom = (scrollBar.getValue() + scrollBar.getVisibleAmount()) == scrollBar.getMaximum();

        SwingUtilities.invokeLater(() -> {
            textArea.append('\n' + message);
            if (atBottom) textAreaCaret.setDot(textArea.getDocument().getLength());
        });
    }


}
