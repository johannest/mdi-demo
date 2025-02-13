package com.example.application.windows;

import com.example.application.components.window.WindowContent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import elemental.json.JsonObject;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
@WindowContent(value = "form", title = "Form", top = "50%", left = "10%", height = "50%", width = "75%")
@PermitAll
public class FormWindow extends FormLayout {
    private Logger logger = LoggerFactory.getLogger(FormWindow.class);
    DatePicker datePicker = new DatePicker("Date");
    ComboBox<String> comboBox = new ComboBox<>("Combo");
    TextField textField = new TextField("Text");

    public FormWindow() {
        Binder<Bean> binder = new Binder<>();
        comboBox.setItems("One", "Two", "Three");
        binder.forField(datePicker).bind(Bean::getDate, Bean::setDate);
        binder.forField(comboBox).bind(Bean::getNumber, Bean::setNumber);
        binder.forField(textField).bind(Bean::getText, Bean::setText);
        binder.addValueChangeListener(e -> {
            Notification.show(e.getValue().toString());
        });
        add(datePicker, comboBox, textField);

        // demonstrates how to capture keydown event on DatePicker
        datePicker.getElement().addEventListener("keydown", event -> {
            JsonObject eventData = event.getEventData();
            if (eventData != null) {
                String key = eventData.getString("event.key");
                String keyCode = eventData.getString("event.code");
                logger.info("Key: {} Code: {}", key, keyCode);
            }
        }).addEventData("event.key").addEventData("event.code");
    }

    public class Bean {
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        private LocalDate date;
        private String text;
        private String number;
    }
}
