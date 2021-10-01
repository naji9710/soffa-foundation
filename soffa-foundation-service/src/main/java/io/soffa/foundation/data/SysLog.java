package io.soffa.foundation.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class SysLog {

    private String id;
    private String kind;
    private String event;
    private String data;
    private Float value;
    private String requestId;
    private String spanId;
    private String traceId;
    private String user;
    private String application;
    private String error;
    private String errorDetails;
    private Date createdAt;
    private long duration;

    public SysLog(String kind, String event) {
        this.kind = kind;
        this.event = event;
    }
}
