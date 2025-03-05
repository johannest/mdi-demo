package com.example.application.windows;

import com.example.application.components.window.EditWindowListener;
import com.example.application.components.window.HasEditWindowListener;
import com.example.application.components.window.WindowContent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.example.application.windows.TextEditWindow.WINDOW_NAME;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
@WindowContent(value = WINDOW_NAME, title = "Edit text value", left = "80%", width = "200x", height = "220px", showInMenu = false)
@PermitAll
public class TextEditWindow extends Div implements HasEditWindowListener<String> {

    public static final String WINDOW_NAME = "text-edit";
    private final TextField editText;

    private EditWindowListener<String> editWindowListener;

    public TextEditWindow() {
        editText = new TextField("Edit value");

        Button saveButton = new Button("Accept", VaadinIcon.CHECK.create(), e -> {
            if (editWindowListener != null) {
                editWindowListener.onSave(editText.getValue());
            }
        });

        Button cancelButton = new Button("Cancel", VaadinIcon.CLOSE.create(), e -> {
            if (editWindowListener != null) {
                editWindowListener.onCancel();
            }
        });

        Div buttonWrap = new Div(cancelButton, saveButton);
        buttonWrap.setWidth("100%");
        add(editText, buttonWrap);
    }

    public void setTextValue(String text) {
        editText.setValue(text);
    }

    @Override
    public void setEditWindowListener(EditWindowListener<String> editWindowListener) {
        this.editWindowListener = editWindowListener;
    }
}
