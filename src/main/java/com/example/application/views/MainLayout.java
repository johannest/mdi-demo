package com.example.application.views;

import com.example.application.components.TopNav;
import com.example.application.components.TopNavItem;
import com.example.application.components.window.WindowFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
@PermitAll
public class MainLayout extends AppLayout implements AfterNavigationObserver {
    private Logger logger = LoggerFactory.getLogger(MainLayout.class);
    private TopNav nav;
    private WindowFactory windowFactory;
    private final AuthenticationContext authenticationContext;

    public MainLayout(WindowFactory windows, AuthenticationContext authenticationContext) {
        this.windowFactory = windows;
        this.authenticationContext = authenticationContext;
        setPrimarySection(Section.DRAWER);
        addHeaderContent();
        setDrawerOpened(false);
    }

    private void addHeaderContent() {
        H1 appName = new H1("MDI Demo");
        appName.addClassNames(LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        addToNavbar(header, createNavigation(), createFooter());
    }


    private SideNav createNavigation() {
        nav = new TopNav();
        nav.setWidth("100%");

        windowFactory.getWindowNames().forEach(name -> {
            if (windowFactory.showWindowInMenu(name)) {
                if (windowFactory.isWindowAllowed(name)) {
                    TopNavItem win = new TopNavItem(windowFactory.getWindowTitle(name),
                            "windows/" + name, LineAwesomeIcon.WINDOWS.create());
                    nav.addItem(win);
                    logger.info("Added window {} to top nav", name);
                } else {
                    logger.info("Windows {} not listed in the navigation due to the security constraint", name);
                }
            }
        });
        TopNavItem base = new TopNavItem("Root menu");
        TopNavItem item = new TopNavItem("Item");
        TopNavItem item2 = new TopNavItem("Another item");
        TopNavItem subItem = new TopNavItem("SubItem");
        subItem.addClassNames("sub");
        base.addItem(item);
        base.addItem(item2);
        item.addItem(subItem);
        nav.addItem(base);

        TopNavItem logout = new TopNavItem("Logout", "logout");
        logout.addClassNames("logout");
        nav.addItem(logout);

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        return layout;
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass()
                .getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String path = event.getLocation().getPath();
        if ("logout".equals(path)) {
            authenticationContext.logout();
        }
        nav.getChildren().forEach(comp -> {
            if (comp instanceof TopNavItem item) {
                if (path.equals(item.getPath())) {
                    item.getElement().setAttribute("active", "true");
                } else {
                    item.getElement().removeAttribute("active");
                }
            }
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        var js = """
                window.addEventListener('beforeunload', (evt) => {
                    const msg = 'foo';
                    (evt || window.event).returnValue = msg;
                    return msg;
                  });
                """;
        attachEvent.getUI().getElement().executeJs(js);
    }
}
