package com.example.application.windows;

import com.example.application.components.window.WindowContent;
import com.example.application.components.window.WindowData;
import com.example.application.components.window.WindowFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
@WindowContent(value = "form", title = "Form", top = "50%", left = "10%", height = "50%", width = "75%")
@PermitAll
public class FormWindow extends FormLayout {

    DatePicker datePicker = new DatePicker("Date");
    ComboBox<String> comboBox = new ComboBox<>("Combo");
    TextField textField = new TextField("Text");
	private WindowFactory factory;

    public FormWindow(WindowFactory factory) {
        this.factory = factory;
		Binder<Bean> binder = new Binder<>();
        comboBox.setItems("One", "Two", "Three");
        binder.forField(datePicker).bind(Bean::getDate, Bean::setDate);
        binder.forField(comboBox).bind(Bean::getNumber, Bean::setNumber);
        binder.forField(textField).bind(Bean::getText, Bean::setText);
        binder.addValueChangeListener(e -> {
            Notification.show(e.getValue().toString());
        });
        add(datePicker, comboBox, textField);
        add(new Button("Show window details", e -> showWindowDetails()));
    }
    
    private void showWindowDetails() {
    	Optional<WindowData> windowData = factory.getWindowDataForView(this);
    	windowData.ifPresent(e -> {
    		Notification.show("WindowName: " + e.getName() + " Number: " + e.getWindowNumber());
    	});
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
