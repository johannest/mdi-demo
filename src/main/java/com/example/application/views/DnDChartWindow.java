package com.example.application.views;

import com.example.application.components.window.WindowContent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
@WindowContent(value = "chart", title = "Drag and drop chart", left = "10%", top = "50px", width = "40%")
public class DnDChartWindow extends VerticalLayout{

    public DnDChartWindow() {
        Chart chart = new Chart(ChartType.COLUMN);
        chart.getConfiguration().setTitle("Chart");
        chart.addClassNames(LumoUtility.Border.ALL,
                LumoUtility.BorderColor.PRIMARY);

        DataSeries series1 = new DataSeries();
        series1.setData(10, 20, 300);
        series1.setName("Series 1");
        DataSeries series2 = new DataSeries();
        series2.setData(100, 30, 50);
        series2.setName("Series 2");
        List<Series> series = new ArrayList<>();
        chart.getConfiguration().setSeries(series);

        chart.setWidth("400px");
        chart.setHeight("300px");

        DropTarget<Chart> chartTarget = DropTarget.create(chart);

        Div series1Div = new Div(VaadinIcon.BAR_CHART_H.create(),
                new Text("Series 1"));
        series1Div.addClassNames(LumoUtility.Display.FLEX,
                LumoUtility.Padding.SMALL, LumoUtility.BoxShadow.SMALL,
                LumoUtility.Gap.SMALL);
        series1Div.setWidth("200px");
        Div series2Div = new Div(VaadinIcon.BAR_CHART_H.create(),
                new Text("Series 2"));
        series2Div.addClassNames(LumoUtility.Display.FLEX,
                LumoUtility.Padding.SMALL, LumoUtility.BoxShadow.SMALL,
                LumoUtility.Gap.SMALL);
        series2Div.setWidth("200px");
        Div trashDiv = new Div(VaadinIcon.TRASH.create());
        trashDiv.addClassNames(LumoUtility.Padding.SMALL,
                LumoUtility.BoxShadow.SMALL);
        trashDiv.setWidth("200px");

        DragSource<Div> series1Source = DragSource.create(series1Div);
        DragSource<Div> series2Source = DragSource.create(series2Div);
        DropTarget<Div> trashTarget = DropTarget.create(trashDiv);

        chartTarget.addDropListener(e -> {
            if (e.getDragSourceComponent().get().equals(series1Div)) {
                if (!chart.getConfiguration().getSeries().contains(series1)) {
                    chart.getConfiguration().addSeries(series1);
                }
            } else if (e.getDragSourceComponent().get().equals(series2Div)) {
                if (!chart.getConfiguration().getSeries().contains(series2)) {
                    chart.getConfiguration().addSeries(series2);
                }
            }
        });

        trashTarget.addDropListener(e -> {
            List<Series> list = new ArrayList<>(
                    chart.getConfiguration().getSeries());
            if (e.getDragSourceComponent().get().equals(series1Div)) {
                list.remove(series1);
                chart.getConfiguration().setSeries(list);
                chart.setConfiguration(chart.getConfiguration());
            } else if (e.getDragSourceComponent().get().equals(series2Div)) {
                list.remove(series2);
                chart.getConfiguration().setSeries(list);
                chart.setConfiguration(chart.getConfiguration());
            }
        });

        add(chart, series1Div, series2Div, trashDiv);
    }

}
