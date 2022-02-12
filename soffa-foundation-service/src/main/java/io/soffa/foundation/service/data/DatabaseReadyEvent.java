package io.soffa.foundation.service.data;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

public class DatabaseReadyEvent extends ApplicationContextEvent {

    public static final long serialVersionUID = 1L;

    public DatabaseReadyEvent(ApplicationContext source) {
        super(source);
    }
}
