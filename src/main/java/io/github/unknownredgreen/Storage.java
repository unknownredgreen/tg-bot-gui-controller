package io.github.unknownredgreen;

import io.github.unknownredgreen.gui.BasicWindow;
import io.github.unknownredgreen.interfaces.HasScrollPane;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Storage {
    private final Map<String, Dimension> windowSizes = new HashMap<>(Map.of(
            "MainWindow", new Dimension(820, 250),
            "DisplayMessagesWindow", new Dimension(400, 600),
            "SendMessagesWindow", new Dimension(500, 500)
    ));
    private final Map<String, Point> windowLocations = new HashMap<>();
    private final Map<String, Map<Long, Integer>> windowScrollValue = new HashMap<>(Map.of(
            "DisplayMessagesWindow", new HashMap<>(),
            "SendMessagesWindow", new HashMap<>()
    ));


    public Dimension getWindowSize(Class<? extends BasicWindow> clazz) {
        return windowSizes.get(clazz.getSimpleName());
    }
    public void saveWindowSize(Class<? extends BasicWindow> clazz, Dimension size) {
        windowSizes.put(clazz.getSimpleName(), size);
    }

    public Point getWindowLocation(Class<? extends BasicWindow> clazz) {
        return windowLocations.get(clazz.getSimpleName());
    }
    public void saveWindowLocation(Class<? extends BasicWindow> clazz, Point location) {
        windowLocations.put(clazz.getSimpleName(), location);
    }

    public Integer getWindowScrollValue(Class<? extends HasScrollPane> clazz, long chatId) {
        return windowScrollValue.get(clazz.getSimpleName()).get(chatId);
    }
    public void saveWindowScrollValue(Class<? extends HasScrollPane> clazz, int scrollValue, long chatId) {
        windowScrollValue.get(clazz.getSimpleName()).put(chatId, scrollValue);
    }
}