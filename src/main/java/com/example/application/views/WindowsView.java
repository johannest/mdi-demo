package com.example.application.views;

import com.example.application.components.window.Window;
import com.example.application.components.window.WindowFactory;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.*;

import java.util.Optional;

@Route(value = "windows", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class WindowsView extends Div
        implements HasUrlParameter<String>, AfterNavigationObserver {

    private String param;
    private WindowFactory windowFactory;

    public WindowsView(WindowFactory windowFactory) {
        this.windowFactory = windowFactory;
    }

    @Override
    public void setParameter(BeforeEvent event,
                             @WildcardParameter String parameter) {
        param = parameter;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String[] wins = param.split("/");
        for (String win : wins) {
            Optional<Window> window = windowFactory.getWindow(win);
            window.ifPresent(Window::open);
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        windowFactory.closeAllWindows();
    }
}
