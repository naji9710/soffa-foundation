package io.soffa.foundation.data;

import io.soffa.foundation.core.RequestContext;
import io.soffa.foundation.exceptions.ErrorUtil;
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

    public void setContext(RequestContext context) {
        if (context != null) {
            setSpanId(context.getSpanId());
            setTraceId(context.getTraceId());
            setUser(context.getUsername().orElse("guest"));
            setApplication(context.getApplicationName());
        }
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setError(Throwable error) {
        if (error != null) {
            setError(ErrorUtil.loookupOriginalMessage(error));
            setErrorDetails(ErrorUtil.getStacktrace(error));
        }
    }
}
