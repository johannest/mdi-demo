package com.example.application.components.window;

public interface EditWindowListener<T> {
    default void onSave(T value) {
    }

    default void onCancel() {
    }
}