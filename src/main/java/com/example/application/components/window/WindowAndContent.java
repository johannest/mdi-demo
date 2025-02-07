package com.example.application.components.window;

import com.vaadin.flow.component.Component;

/**
 * Simple record to transfer Window and its actual Vaadin component content
 *
 * @param window  window instance
 * @param content window's Vaadin component content
 */
public record WindowAndContent(Window window, Component content) {
}
