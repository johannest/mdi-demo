package com.example.application.views;

import com.example.application.components.window.WindowContent;
import com.example.application.components.window.WindowFactory;
import com.example.application.data.Person;
import com.example.application.data.PersonService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@WindowContent(value = "persons", title = "Person List", top = "50%", left = "10%", height = "50%", width = "75%")
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
            Button button = new Button(person.getMaritalStatus().toString());
            button.addClickListener(event -> {
                // TODO create MaritalStatusEditorWindow and open it for editing person's marital status
            });
            return button;
        });
        add(personGrid);
    }

    @PostConstruct
    public void init() {
        personGrid.setItems(personService.fetchAll());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        windowFactory = applicationContext.getBean(WindowFactory.class);
    }
}

