package com.example.application.windows;

import com.example.application.components.window.Window;
import com.example.application.components.window.WindowContent;
import com.example.application.components.window.WindowData;
import com.example.application.components.window.WindowFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Window management view listing all created windows, with possibility to open those
 */
@UIScope
@Component("windowManagerWindow")
@WindowContent(value = "open-windows", title = "Windows Manager", left = "0%", top = "50px", width = "33%", height = "33%", multiWindow = false, showInManager = false)
@RolesAllowed({"ADMIN","USER"})
public class WindowManagerWindow extends Div {

    private final Grid<WindowData> windowGrid;
    private final ApplicationContext applicationContext;
    private WindowFactory windowFactory;

    public WindowManagerWindow(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        windowGrid = new Grid<>(WindowData.class, false);
        windowGrid.addColumn(WindowData::getTitle).setHeader("Title");
        windowGrid.addColumn(WindowData::getWindowNumber).setHeader("Window Number");
        windowGrid.addComponentColumn(windowData -> {
            Button openButton = new Button("Focus", e -> {
                Window window = windowFactory.getOrCreateWindow(windowData.getName(), windowData.getWindowNumber());
                if (window.isMini()) {
                    window.restore();
                    window.setPosition(windowData.getWindowContent().left(), windowData.getWindowContent().top());
                } else {
                    window.bringToFront();
                }
            });
            openButton.setIcon(VaadinIcon.ARROW_FORWARD.create());
            return openButton;
        });
        windowGrid.addComponentColumn(windowData -> {
            Button openButton = new Button("Hide", e -> {
                Window window = windowFactory.getOrCreateWindow(windowData.getName(), windowData.getWindowNumber());
                window.minimize();
            });
            openButton.setIcon(VaadinIcon.ARROW_DOWN.create());
            return openButton;
        });
        windowGrid.setHeight("300px");
        add(windowGrid);

    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        windowFactory = applicationContext.getBean(WindowFactory.class);
        windowFactory.addWindowCreatedListener(e -> {
            if (windowGrid.isAttached()) {
                attachEvent.getUI().access(() -> windowGrid.setItems(windowFactory.getOpenedWindows()));
            }
        });
        windowGrid.setItems(windowFactory.getOpenedWindows());
    }
}
