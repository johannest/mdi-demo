package com.example.application.windows;

import com.example.application.components.window.Window;
import com.example.application.components.window.WindowAndContent;
import com.example.application.components.window.WindowContent;
import com.example.application.components.window.WindowFactory;
import com.example.application.data.Person;
import com.example.application.data.PersonService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@WindowContent(value = "persons", title = "Person List", top = "20%", left = "10%", height = "50%", width = "75%")
@RolesAllowed("ADMIN")
public class PersonListWindow extends Div {

    private PersonService personService;
    private final Grid<Person> personGrid;
    private WindowFactory windowFactory;
    private ApplicationContext applicationContext;

    public PersonListWindow(ApplicationContext applicationContext, PersonService personService) {
        this.applicationContext = applicationContext;
        this.personService = personService;

        personGrid = new Grid<>(Person.class, false);
        personGrid.addColumn(Person::getFirstName).setHeader("First Name");
        personGrid.addColumn(Person::getLastName).setHeader("Last Name");
        personGrid.addComponentColumn(person -> {
            Div maritalStatusValue = new Div(person.getMaritalStatus().name());
            Button button = new Button();
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            button.setIcon(VaadinIcon.EDIT.create());
            button.addClickListener(event -> {
                Optional<WindowAndContent> windowAndContent = windowFactory.getWindowInstance(MaritalStatusEditWindow.WINDOW_NAME);
                windowAndContent.ifPresent(windowNContent -> {
                    createEditMaritalStatusEditWindow(person, windowNContent);
                });
            });
            Div wrap = new Div(maritalStatusValue, button);
            wrap.addClassNames(LumoUtility.Display.FLEX);
            return wrap;
        });
        add(personGrid);
    }

    private void createEditMaritalStatusEditWindow(Person person, WindowAndContent windowAndContent) {
        Window window = windowAndContent.window();
        window.open();
        com.vaadin.flow.component.Component windowContent = windowAndContent.content();
        if (windowContent instanceof MaritalStatusEditWindow maritalStatusEditWindow) {
            // set person such that we will know whose status was changed
            maritalStatusEditWindow.setPerson(person);
        }
        window.addOpenedChangeListener(e -> {
           refresh();
        });
        windowFactory.addBeanToWindow(person, window);
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    private void refresh() {
        personGrid.setItems(personService.fetchAll());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        windowFactory = applicationContext.getBean(WindowFactory.class);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        windowFactory = null;
    }
}

