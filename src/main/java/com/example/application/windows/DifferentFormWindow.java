package com.example.application.windows;

import com.example.application.components.window.EditWindowListener;
import com.example.application.components.window.WindowAndContent;
import com.example.application.components.window.WindowContent;
import com.example.application.components.window.WindowFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
@WindowContent(value = "form2", title = "Second Form", left = "50%", height = "50%", width = "45%")
@PermitAll
public class DifferentFormWindow extends VerticalLayout {
    TextField textField1 = new TextField("First name");
    TextField textField2 = new TextField("Last name");
    TextField textField3 = new TextField("Initials");

    public DifferentFormWindow(WindowFactory windowFactory) {
        textField1.setReadOnly(true);
        textField2.setReadOnly(true);
        textField3.setReadOnly(true);

        Button button1 = new Button("", VaadinIcon.PENCIL.create(), e -> createClickListener(windowFactory, textField1));
        Button button2 = new Button("", VaadinIcon.PENCIL.create(), e -> createClickListener(windowFactory, textField2));
        Button button3 = new Button("", VaadinIcon.PENCIL.create(), e -> createClickListener(windowFactory, textField3));

        HorizontalLayout hl1 = new HorizontalLayout(textField1, button1);
        HorizontalLayout hl2 = new HorizontalLayout(textField2, button2);
        HorizontalLayout hl3 = new HorizontalLayout(textField3, button3);

        hl1.setAlignSelf(Alignment.END, button1);
        hl2.setAlignSelf(Alignment.END, button2);
        hl3.setAlignSelf(Alignment.END, button3);

        add(hl1, hl2, hl3);
    }

    private void createClickListener(WindowFactory windowFactory, TextField textField) {
        Optional<WindowAndContent> modalWindowInstance = windowFactory.getModalWindowInstance(DifferentFormWindow.this, TextEditWindow.WINDOW_NAME);
        modalWindowInstance.ifPresent(window -> {
            window.window().open();
            if (window.content() instanceof TextEditWindow textEditWindow) {
                // set the current value to modal edit window
                textEditWindow.setValue(textField.getValue());
                // add listener
                textEditWindow.setEditWindowListener(new EditWindowListener<>() {
                    @Override
                    public void onSave(String value) {
                        // "save" value
                        textField.setValue(value);
                        window.window().close();
                    }

                    @Override
                    public void onCancel() {
                        // just close the modal window
                        window.window().close();
                    }
                });
            }
        });
    }
}
