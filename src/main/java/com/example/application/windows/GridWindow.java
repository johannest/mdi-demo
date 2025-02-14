package com.example.application.windows;

import com.example.application.components.window.WindowContent;
import com.example.application.components.window.WindowData;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.LitRenderer;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
@WindowContent(value = "grid", title = "Two Grids", left = "50%", top = "50px", width = "50%", height = "50%")
@RolesAllowed("USER")
public class GridWindow extends HorizontalLayout {

    public GridWindow() {

        List<String> items1 = IntStream.range(0, 1000)
                .mapToObj(i -> "Item " + i).collect(Collectors.toList());
        List<Integer> items2 = IntStream.range(1001, 2000).mapToObj(i -> i)
                .collect(Collectors.toList());

        Grid<String> grid1 = new Grid<>();
        grid1.addColumn(LitRenderer.<String> of(
                "<span><b style='color: blue'>${item.number}</b></span>")
                .withProperty("number", i -> i.toString()));
        GridListDataView<String> dataView1 = grid1.setItems(items1);

        Grid<Integer> grid2 = new Grid<>();
        grid2.addColumn(i -> i.toString())
                .setPartNameGenerator(i -> i < 1500 ? "red" : null);
        GridListDataView<Integer> dataView2 = grid2.setItems(items2);
        
        add(new Button("Show window details", e -> showWindowDetails()));
        add(grid1, grid2);
    }
    
    private void showWindowDetails() {
    	WindowData windowData = ComponentUtil.getData(this, WindowData.class);
    	if (windowData != null) {
    		Notification.show("WindowName: " + windowData.getName() + " Number: " + windowData.getWindowNumber());
    	};
    }

}
