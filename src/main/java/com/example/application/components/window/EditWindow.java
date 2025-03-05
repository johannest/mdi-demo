package com.example.application.components.window;

/**
 * Interface for windows providing edit capabilities
 *
 * @param <T> type of the editable data
 */
public interface EditWindow<T> {

    void setValue(T value);

    void setEditWindowListener(EditWindowListener<T> editWindowListener);
}
