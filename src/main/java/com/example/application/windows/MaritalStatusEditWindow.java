package com.example.application.windows;

import com.example.application.components.window.WindowContent;
import com.example.application.data.Person;
import com.example.application.event.MaritalStatusCancelEditEvent;
import com.example.application.event.MaritalStatusSavedEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.example.application.windows.MaritalStatusEditWindow.WINDOW_NAME;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
@WindowContent(value = WINDOW_NAME, title = "Select Marital Status", left = "50%", width = "300x", height = "210px", showInMenu = false)
public class MaritalStatusEditWindow extends Div {

    public static final String WINDOW_NAME = "marital-status";
    private final ListBox<Person.MaritalStatus> maritalStatusList;
    private final Span title;
    private Person person;

    public MaritalStatusEditWindow(ApplicationEventPublisher applicationEventPublisher) {
        addClassName(LumoUtility.Padding.MEDIUM);
        maritalStatusList = new ListBox<>();
        maritalStatusList.setItems(Person.MaritalStatus.values());
        maritalStatusList.setValue(Person.MaritalStatus.MARRIED);

        Button saveButton = new Button("Accept", VaadinIcon.CHECK.create(), e -> {
            MaritalStatusSavedEvent maritalStatusSavedEvent = new MaritalStatusSavedEvent(this);
            maritalStatusSavedEvent.setPerson(person);
            maritalStatusSavedEvent.setMaritalStatus(maritalStatusList.getValue());
            applicationEventPublisher.publishEvent(maritalStatusSavedEvent);
        });

        Button cancelButton = new Button("Cancel", VaadinIcon.CLOSE.create(), e -> {
            MaritalStatusCancelEditEvent maritalStatusCancelEditEvent = new MaritalStatusCancelEditEvent(this);
            maritalStatusCancelEditEvent.setPerson(person);
            applicationEventPublisher.publishEvent(maritalStatusCancelEditEvent);
        });

        Div buttonWrap = new Div(cancelButton, saveButton);
        buttonWrap.setWidth("100%");
        title = new Span();
        add(title, maritalStatusList, buttonWrap);
    }

    public void setPerson(Person person) {
        this.person = person;
        if (person != null) {
            title.setText("Select marital status of "+person.getFullName());
            maritalStatusList.setValue(person.getMaritalStatus());
        }
    }
}
