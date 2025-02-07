package com.example.application.event;

import com.example.application.data.Person;
import org.springframework.context.ApplicationEvent;

public class MaritalStatusSavedEvent extends ApplicationEvent {

    private Person person;
    private Person.MaritalStatus maritalStatus;

    public MaritalStatusSavedEvent(Object source) {
        super(source);
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Person.MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(Person.MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }
}
