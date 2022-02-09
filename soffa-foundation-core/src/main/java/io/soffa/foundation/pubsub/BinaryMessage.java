package io.soffa.foundation.pubsub;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class BinaryMessage {

    private String sid;
    private Object data;
    private Map<String, String> headers;

}
