package io.github.unknownredgreen.gui;

import io.github.unknownredgreen.Bot;
import io.github.unknownredgreen.Storage;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class BasicWindow extends JFrame {
    @Setter
    protected static Bot bot;
    @Setter
    protected static Storage storage;
    @Setter
    private BasicWindow otherWindowToClose;
    private final Class<? extends BasicWindow> thisClass = this.getClass();

    public BasicWindow () {
        setTitle("bot controller :: %s".formatted(thisClass.getSimpleName()));
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);
        setDefaultCloseOperation(getCloseOperation());
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (otherWindowToClose != null) {
                    otherWindowToClose.dispose();
                }
                storage.saveWindowLocation(thisClass, getLocation());
                onWindowClosing();
                dispose();
            }
        });
        Dimension storedSize = storage.getWindowSize(thisClass);
        setSize(storedSize);
        Point storedLocation = storage.getWindowLocation(thisClass);
        if (storedLocation != null) setLocation(storedLocation);

        setVisible(true);

        onInitialization();
        revalidate(); repaint();
    }

    @Override
    public void dispose() {
        storage.saveWindowLocation(thisClass, getLocation());
        onWindowClosing();
        super.dispose();
    }

    protected int getCloseOperation() {return DISPOSE_ON_CLOSE;}
    protected abstract void onInitialization();
    protected void onWindowClosing() {}
}
