package io.github.unknownredgreen.gui;

import org.telegram.telegrambots.meta.api.objects.Chat;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends BasicWindow {
    private SendMessagesWindow sendMessagesWindow;
    private DisplayMessagesWindow displayMessagesWindow;
    private JPanel buttons;
    private static final Dimension buttonSize = new Dimension(200, 75);
    private static final Insets buttonMargin = new Insets(2, 2, 2, 2);

    public MainWindow() {
        bot.setMainWindow(this);
    }

    @Override
    protected void onInitialization() {
        buttons = new JPanel();
        buttons.setLayout(new GridLayout(0, 3, 10, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (var value : bot.getActiveChats().values()) {
            addNewButton(value);
        }

        JScrollPane scrollPane = new JScrollPane(buttons);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(620, 300));
        scrollPane.getVerticalScrollBar().setUnitIncrement(30);

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected int getCloseOperation() {
        return EXIT_ON_CLOSE;
    }

    private void addNewButton(Chat chat) {
        String chatUserName = chat.getUserName();

        String chatDisplayName = null;

        if (chat.isUserChat()) {
            String firstName = chat.getFirstName();
            String lastName = chat.getLastName();

            if (firstName != null) chatDisplayName = firstName;

            if (lastName != null) {
                if (chatDisplayName != null) {
                    chatDisplayName += " " + lastName;
                }
                else {
                    chatDisplayName = lastName;
                }
            }
        } else {
            chatDisplayName = chat.getTitle();
        }
        if (chatDisplayName != null) chatDisplayName = chatDisplayName.replace("<", "< ").replace(">", " >");

        long chatId = chat.getId();

        JButton button = new JButton();
        button.setPreferredSize(buttonSize);
        button.setMargin(buttonMargin);

        if (chatUserName != null) {
            button.setText("<html>%s<br>@%s<br>ID:%d</html>".formatted(chatDisplayName, chatUserName, chatId));
        } else {
            button.setText("<html>%s<br>ID:%d</html>".formatted(chatDisplayName, chatId));
        }

        String finalChatDisplayName = chatDisplayName;
        button.addActionListener(e -> {
            closeDisplayAndSendWindows();
            displayMessagesWindow = new DisplayMessagesWindow(chatId, finalChatDisplayName);
            sendMessagesWindow = new SendMessagesWindow(chatId, finalChatDisplayName);
            displayMessagesWindow.setOtherWindowToClose(sendMessagesWindow);
            sendMessagesWindow.setOtherWindowToClose(displayMessagesWindow);
        });

        buttons.add(button);
    }

    private void closeDisplayAndSendWindows() {
        if (displayMessagesWindow != null) displayMessagesWindow.dispose();
        if (sendMessagesWindow != null) sendMessagesWindow.dispose();
    }

    public void newChatDetected(Chat chat) {
        SwingUtilities.invokeLater(() -> {
            addNewButton(chat);
            buttons.revalidate();
            buttons.repaint();
        });
    }
}