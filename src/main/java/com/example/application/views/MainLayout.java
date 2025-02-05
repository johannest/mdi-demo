package com.example.application.views;

import com.example.application.components.TopNav;
import com.example.application.components.TopNavItem;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private TopNav nav;
    private WindowFactory windowFactory;

    public MainLayout(WindowFactory windows) {
        this.windowFactory = windows;
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

        windowFactory.getWindowNames().forEach(name -> {
        	TopNavItem win = new TopNavItem(windowFactory.getWindowTitle(name),
                    "windows/" + name, LineAwesomeIcon.WINDOWS.create());
            nav.addItem(win);
        });
        TopNavItem base = new TopNavItem("Root menu");
        TopNavItem item = new TopNavItem("Item");
        TopNavItem item2 = new TopNavItem("Another item");
        TopNavItem subItem = new TopNavItem("SubItem");
        base.addItem(item);
        base.addItem(item2);
        item.addItem(subItem);
        nav.addItem(base);
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
    	// TODO this probably doesn't make sense, with multiple windows highlighting becomes kind of useless 
        String path = event.getLocation().getPath();
        nav.getChildren().forEach(comp -> {
            if (comp instanceof TopNavItem) {
            	TopNavItem item = (TopNavItem) comp;
                if (path.equals(item.getPath())) {
                    item.getElement().setAttribute("active", "true");
                } else {
                    item.getElement().removeAttribute("active");
                }
            }
        });
    }
}
