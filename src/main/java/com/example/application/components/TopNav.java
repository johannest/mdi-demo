package com.example.application.components;

import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.sidenav.SideNav;

public class TopNav extends SideNav implements HasTheme {
	
	public static String TOP_THEME = "top";

    public TopNav() {
        addThemeName(TOP_THEME);
    }

    public TopNav(String label) {
        super(label);
        addThemeName(TOP_THEME);
    }

}
