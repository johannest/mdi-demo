package com.example.application.components.window;

/**
 * Window (meta) data for window's name, title, instance number, and instance reference
 */
public class WindowData {
    private String name;
    private String title;
    private Integer windowNumber;
    private WindowContent windowContent;
    private Window instance;

    public WindowData(String name, String title, Integer windowNumber, WindowContent windowContent, Window instance) {
        this.name = name;
        this.title = title;
        this.windowNumber = windowNumber;
        this.windowContent = windowContent;
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getWindowNumber() {
        return windowNumber;
    }

    public void setWindowNumber(Integer windowNumber) {
        this.windowNumber = windowNumber;
    }

    public WindowContent getWindowContent() {
        return windowContent;
    }

    public void setWindowContent(WindowContent windowContent) {
        this.windowContent = windowContent;
    }

    public Window getInstance() {
        return instance;
    }

    public void setInstance(Window instance) {
        this.instance = instance;
    }
}