package com.example.application.event;

import com.example.application.data.Person;
import org.springframework.context.ApplicationEvent;

public class MaritalStatusCancelEditEvent  extends ApplicationEvent {
    private Person person;

    public MaritalStatusCancelEditEvent(Object source) {
        super(source);
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
