package com.example.application.views;

import com.example.application.components.window.WindowAndContent;
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
            Optional<WindowAndContent> windowAndContent = windowFactory.getOrCreateWindow(win);
            windowAndContent.ifPresent(pair -> pair.window().open());
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        windowFactory.closeAllWindows();
    }
}
