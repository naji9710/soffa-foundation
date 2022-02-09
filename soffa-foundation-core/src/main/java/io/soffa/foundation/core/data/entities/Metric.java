package io.soffa.foundation.core.data.entities;

import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.context.RequestContextHolder;
import io.soffa.foundation.core.RequestContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metric {

    private String id;
    private String name;
    private String data;
    private double value;
    private String spanId;
    private String traceId;
    private String userId;
    private String application;
    private Date date;
    private int year;
    private int month;
    private int day;
    private int processingFlag;


    public Metric(String name, double value) {
        this.name = name;
        this.value = value;
        populateContext(RequestContextHolder.get().orElse(null));
    }

    public Metric(String name, double value, Object data) {
        this(name, value);
        this.data = JsonUtil.serialize(data);
    }


    public Metric(String name, double value, RequestContext context) {
        this(name, value);
        populateContext(context);
    }

    private void populateContext(RequestContext context) {
        if (context == null) {
            return;
        }
        this.application = context.getApplicationName();
        this.userId = context.getUsername().orElse(null);
        this.traceId = context.getTraceId();
        this.spanId = context.getSpanId();
    }
}


