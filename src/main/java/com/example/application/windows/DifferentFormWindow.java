package com.example.application.windows;

import com.example.application.components.window.WindowContent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
@WindowContent(value = "form2", title = "Second Form", left = "50%", height = "50%", width = "45%")
@PermitAll
public class DifferentFormWindow extends VerticalLayout {
    TextField textField1 = new TextField("First name");
    TextField textField2 = new TextField("Last name");
    TextField textField3 = new TextField("Initials");

    public DifferentFormWindow() {
        add(textField1, textField2, textField3);
    }
}
