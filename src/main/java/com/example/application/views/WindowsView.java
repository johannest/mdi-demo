package com.example.application.views;

import com.example.application.components.window.WindowAndContent;
import com.example.application.components.window.WindowFactory;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Route(value = "windows", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class WindowsView extends Div
        implements HasUrlParameter<String>, AfterNavigationObserver {

    private Logger logger = LoggerFactory.getLogger(WindowsView.class);
    private String param;
    private WindowFactory windowFactory;
    private final AuthenticationContext authenticationContext;

    public WindowsView(WindowFactory windowFactory, AuthenticationContext authenticationContext) {
        this.windowFactory = windowFactory;
        this.authenticationContext = authenticationContext;
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
            if (windowFactory.isWindowAllowed(authenticationContext, win)) {
                Optional<WindowAndContent> windowAndContent = windowFactory.getOrCreateWindow(win);
                // check RolesAllowed
                windowAndContent.ifPresent(pair -> pair.window().open());
            } else if (win != null && !win.isEmpty()) {
                String userName = authenticationContext.getPrincipalName().orElse("?");
                logger.error("User {} tried to access restricted windows {}", userName, win);
            }
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        windowFactory.closeAllWindows();
    }
}
