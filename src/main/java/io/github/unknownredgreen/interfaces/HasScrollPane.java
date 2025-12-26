package io.github.unknownredgreen.interfaces;

import io.github.unknownredgreen.Storage;

import javax.swing.*;
import java.awt.*;

public interface HasScrollPane {
    default void loadScrollState(JScrollPane scrollPane, long chatId, Storage storage) {
        Integer savedScroll = storage.getWindowScrollValue(this.getClass(), chatId);

        //if (savedScroll != null) scrollBar.setValue(savedScroll); not working for some reason
        JViewport viewPort = scrollPane.getViewport();
        if (savedScroll != null) viewPort.setViewPosition(new Point(viewPort.getX(), savedScroll));
    }
    default void saveScrollState(JScrollBar scrollBar, long chatId, Storage storage) {
        storage.saveWindowScrollValue(this.getClass(), scrollBar.getValue(), chatId);
    }
}