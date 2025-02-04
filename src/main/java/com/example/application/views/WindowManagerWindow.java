package com.example.application.views;

import com.example.application.components.window.Window;
import com.example.application.components.window.WindowData;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Window management view listing all created windows, with possibility to open those
 */
// TODO I think we only need one instance of these,
//  but at the moment this is not properly handled: it will open an empty window if navigating here again
@UIScope
@Component("windowManagerWindow")
@WindowContent(value = "open-windows", title = "Windows Manager", left = "0%", top = "0px", width = "50%", height = "50%")
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
                Window window = windowFactory.getWindow(windowData.getName(), windowData.getWindowNumber());
                window.open();
                // TODO bring the window to the TOP (high priority)
            });
            openButton.setIcon(VaadinIcon.ARROW_FORWARD.create());
            return openButton;
        });
        windowGrid.setHeight("300px");
        add(windowGrid);

        // TODO update the list automatically with push (lower priority)
        add(new Button("Refresh", VaadinIcon.REFRESH.create(), e -> {
            windowGrid.setItems(windowFactory.getOpenedWindows());
        }));
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        windowFactory = applicationContext.getBean(WindowFactory.class);
        windowGrid.setItems(windowFactory.getOpenedWindows());
    }
}
