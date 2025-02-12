package com.example.application.components.window;

import com.vaadin.flow.component.Component;

/**
 * Simple record to transfer WindowContent annotation and its actual Vaadin component Class
 *
 * @param windowContent WindowContent annotation of the content class
 * @param windowContentClass Vaadin Component Class for the window's content
 */
public record WindowContentAndClass(WindowContent windowContent, Class<? extends Component> windowContentClass) {
}
