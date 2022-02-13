package io.soffa.foundation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ack {

    public static final Ack OK = new Ack("OK", true);

    private String value;
    private boolean success;

    public Ack(boolean success) {
        this.success = success;
    }

    public Ack(String value) {
        this.value = value;
    }
}
