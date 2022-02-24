package io.soffa.foundation.core.models;

import io.soffa.foundation.annotations.JsonModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonModel
@NoArgsConstructor
@AllArgsConstructor
public class Ack {

    public static final Ack OK = new Ack("OK");

    private String status;
    private String message;

    public Ack(String status) {
        this.status = status;
    }
}
