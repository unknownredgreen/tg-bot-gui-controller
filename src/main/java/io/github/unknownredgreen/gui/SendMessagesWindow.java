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

    private JTextArea titleArea;

    private JTextArea textArea;
    private JScrollPane scrollPane;
    private JScrollBar scrollBar;

    private JPanel bottomPanel;
    private JTextArea outputTextArea;
    private JCheckBox clearAfterSendingCB;
    private JTextField messageToReplyIdInput;


    public SendMessagesWindow(long chatId, String chatTitle) {
        this.chatId = chatId;
        this.chatTitle = chatTitle;
        bot.setSendMessagesWindow(this);
    }

    @Override
    protected void onInitialization() {
        createUpperComponents();
        createCenterComponents();
        createBottomComponents();
        createLowestComponents();

        SwingUtilities.invokeLater(() -> {
            String cutChatTitle = Strings.cutString(chatTitle, 50);
            titleArea.setText("%s\n(ID:%d)".formatted(cutChatTitle, chatId));

            loadScrollState(scrollPane, chatId, storage);
        });
    }

    private void createUpperComponents() {
        titleArea = new JTextArea();
        titleArea.setEditable(false);

        add(titleArea, BorderLayout.NORTH);
    }

    private void createCenterComponents() {
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        DefaultCaret textAreaCaret = (DefaultCaret) textArea.getCaret();
        textAreaCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollBar = scrollPane.getVerticalScrollBar();

        add(scrollPane, BorderLayout.CENTER);
    }

    private void createBottomComponents() {
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        outputTextArea = new JTextArea(3, 40);
        outputTextArea.setLineWrap(true);
        outputTextArea.setEditable(false);
        outputTextArea.setWrapStyleWord(true);

        JScrollPane lowerScroll = new JScrollPane(outputTextArea);
        bottomPanel.add(lowerScroll);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void createLowestComponents() {
        String placeholder = "Message id to reply";


        clearAfterSendingCB = new JCheckBox("Clear after sending", true);
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        checkboxPanel.add(clearAfterSendingCB);
        bottomPanel.add(checkboxPanel);

        //Send button
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

        //Reply to message button
        JButton replyToMessageButton = new JButton("Reply to message");
        replyToMessageButton.addActionListener(e -> {
            Integer messageToReply = null;
            String messageToReplyString = messageToReplyIdInput.getText();

            if (messageToReplyString.isEmpty() || messageToReplyString.equals(placeholder)) {
                outputTextArea.setText("Tried replying but there was no message id where to reply");
                return;
            }

            try {
                messageToReply = Integer.valueOf(messageToReplyString);
            } catch (NumberFormatException ignored) {
                outputTextArea.setText("Message reply id has wrong number format");
            }

            String result = null;
            if (messageToReply != null) result = bot.sendMessage(textArea.getText(), chatId, messageToReply);

            if (result != null) {
                outputTextArea.setText(result);
            } else {
                if (messageToReply == null) {
                    outputTextArea.setText("Message id to reply is too big for Integer");
                }
                else {
                    outputTextArea.setText("Message sent");
                    if (clearAfterSendingCB.isSelected()) {
                        textArea.setText("");
                    }
                }
            }
        });

        //Message id to reply input
        messageToReplyIdInput = new JTextField(20);

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

        buttonPanel.add(sendButton);
        buttonPanel.add(replyToMessageButton);
        buttonPanel.add(messageToReplyIdInput);
        bottomPanel.add(buttonPanel);
    }

    @Override
    protected void onWindowClosing() {
        saveScrollState(scrollBar, chatId, storage);
    }
}
