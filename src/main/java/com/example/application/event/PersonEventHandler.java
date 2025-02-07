package com.example.application.event;

import com.example.application.components.window.WindowFactory;
import com.example.application.data.PersonService;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class PersonEventHandler {
    private final WindowFactory windowFactory;
    private final PersonService personService;

    public PersonEventHandler(WindowFactory windowFactory,
                              PersonService personService) {

        this.windowFactory = windowFactory;
        this.personService = personService;
    }

    @EventListener
    public void handleMaritalStatusSavedEvent(MaritalStatusSavedEvent event) {
        windowFactory.closeWindowOfBean(event.getPerson());
        event.getPerson().setMaritalStatus(event.getMaritalStatus());
        personService.save(event.getPerson());
    }

    @EventListener
    public void handleMaritalStatusCancelEvent(MaritalStatusCancelEditEvent event) {
        windowFactory.closeWindowOfBean(event.getPerson());
    }
}
