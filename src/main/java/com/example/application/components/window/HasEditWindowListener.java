package com.example.application.components.window;

@FunctionalInterface
public interface HasEditWindowListener<T> {

    void setEditWindowListener(EditWindowListener<T> editWindowListener);
}
