package io.soffa.foundation.models.commons;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Ack {

    private String status;
    private boolean success;

    public static final Ack OK = new Ack("OK", true);
}
