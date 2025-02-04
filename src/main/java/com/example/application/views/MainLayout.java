package com.example.application.views;

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

    private H2 viewTitle;
    private SideNav nav;
    private WindowFactory windowFactory;

    public MainLayout(WindowFactory windows) {
        this.windowFactory = windows;
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
        setDrawerOpened(false);
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("MDI Demo");
        appName.addClassNames(LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    // TODO replace SideNav with top navigation for example with https://v-herd.eu/jonte-vaadinplus/top-nav or by Vaadin AppLayout (high priority)
    private SideNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        nav = new SideNav();

        windowFactory.getWindowNames().forEach(name -> {
            SideNavItem win = new SideNavItem(windowFactory.getWindowTitle(name),
                    "windows/" + name, LineAwesomeIcon.WINDOWS.create());
            nav.addItem(win);
        });

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
        viewTitle.setText(getCurrentPageTitle());
        nav.getChildren().forEach(comp -> {
            if (comp instanceof SideNavItem) {
                SideNavItem item = (SideNavItem) comp;
                if (path.equals(item.getPath())) {
                    item.getElement().setAttribute("active", "true");
                } else {
                    item.getElement().removeAttribute("active");
                }
            }
        });
    }
}
