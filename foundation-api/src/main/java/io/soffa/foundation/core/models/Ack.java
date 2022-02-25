package io.soffa.foundation.core.models;

import io.soffa.foundation.annotations.JsonModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@JsonModel
@NoArgsConstructor
@AllArgsConstructor
public class Ack {

    public static final Ack OK = new Ack("OK");

    private String status;
    private String message;
    private Map<String,Object> metadata;

    public Ack(String status) {
        this.status = status;
    }

    public Ack(String status, String message) {
        this.status = status;
        this.message = message;
    }


}
