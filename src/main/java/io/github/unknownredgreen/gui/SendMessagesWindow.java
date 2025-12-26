package io.github.unknownredgreen.gui;

import io.github.unknownredgreen.interfaces.HasScrollPane;
import io.github.unknownredgreen.util.Strings;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;

public class SendMessagesWindow extends BasicWindow implements HasScrollPane {
    private final long chatId;
    private final String chatTitle;
    private JTextArea textArea;
    private JScrollBar scrollBar;
    private DefaultCaret textAreaCaret;

    public SendMessagesWindow(long chatId, String chatTitle) {
        this.chatId = chatId;
        this.chatTitle = chatTitle;
        bot.setSendMessagesWindow(this);
    }

    @Override
    protected void onInitialization() {
        //I`ll refactor this later but this version is working at least
        JTextArea titleArea = new JTextArea();
        titleArea.setEditable(false);

        add(titleArea, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textAreaCaret = (DefaultCaret) textArea.getCaret();
        textAreaCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollBar = scrollPane.getVerticalScrollBar();

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JTextArea outputTextArea = new JTextArea(3, 40);
        outputTextArea.setLineWrap(true);
        outputTextArea.setEditable(false);
        outputTextArea.setWrapStyleWord(true);

        JScrollPane lowerScroll = new JScrollPane(outputTextArea);
        bottomPanel.add(lowerScroll);

        JCheckBox clearAfterSendingCB = new JCheckBox("Clear after sending", true);

        String placeholder = "Message id to reply";
        JTextField messageToReplyIdInput = new JTextField(20);


        ((AbstractDocument) messageToReplyIdInput.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if ((string != null && string.matches("\\d+")) || Objects.equals(string, placeholder)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if ((text != null && text.matches("\\d*")) || Objects.equals(text, placeholder)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        messageToReplyIdInput.setText(placeholder);
        messageToReplyIdInput.setForeground(Color.GRAY);

        messageToReplyIdInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (messageToReplyIdInput.getText().equals(placeholder)) {
                    messageToReplyIdInput.setText("");
                    messageToReplyIdInput.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (messageToReplyIdInput.getText().isEmpty()) {
                    messageToReplyIdInput.setText(placeholder);
                    messageToReplyIdInput.setForeground(Color.GRAY);
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String result = bot.sendMessage(textArea.getText(), chatId);
            if (result != null) {
                outputTextArea.setText(result);
            } else {
                outputTextArea.setText("Message sent");
                if (clearAfterSendingCB.isSelected()) {
                    textArea.setText("");
                }
            }
        });
        JButton replyToMessageButton = new JButton("Reply to message");
        replyToMessageButton.addActionListener(e -> {
            Integer messageToReply = null;
            try {
                messageToReply = Integer.valueOf(messageToReplyIdInput.getText());
            } catch (NumberFormatException ignored) {outputTextArea.setText("Message reply id has wrong number format");}
            String result = null;
            if (messageToReply != null) result = bot.sendMessage(textArea.getText(), chatId, messageToReply);
            if (result != null) {
                outputTextArea.setText(result);
            } else {
                outputTextArea.setText("Message sent");
                if (clearAfterSendingCB.isSelected()) {
                    textArea.setText("");
                }
            }
        });

        buttonPanel.add(sendButton);
        buttonPanel.add(replyToMessageButton);
        buttonPanel.add(messageToReplyIdInput);
        bottomPanel.add(buttonPanel);

        JPanel replyIdAndClearCheckbox = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        replyIdAndClearCheckbox.add(clearAfterSendingCB);
        bottomPanel.add(replyIdAndClearCheckbox);

        add(bottomPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            String cutChatTitle = Strings.cutString(chatTitle, 50);
            titleArea.setText("%s\n(ID:%d)".formatted(cutChatTitle, chatId));

            loadScrollState(scrollPane, chatId, storage);
        });
    }

    @Override
    protected void onWindowClosing() {
        saveScrollState(scrollBar, chatId, storage);
    }
}
