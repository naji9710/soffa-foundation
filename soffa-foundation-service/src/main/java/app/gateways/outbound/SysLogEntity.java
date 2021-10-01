package app.gateways.outbound;

import io.soffa.foundation.data.SysLog;
import io.soffa.foundation.lang.TextUtil;
import io.soffa.foundation.support.Generator;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sys_logs")
@Data
class SysLogEntity {

    @Id
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
    private long duration;
    private String error;
    private String errorDetails;
    @Temporal(TemporalType.TIME)
    private Date createdAt;

    @PrePersist
    public void onPrePersist() {
        if (TextUtil.isEmpty(id)) {
            id = Generator.shortId("slog_");
        }
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    public static SysLogEntity fromDomain(SysLog model) {
        SysLogEntity entity = new SysLogEntity();
        BeanUtils.copyProperties(model, entity);
        return entity;
    }

    public SysLog toDomain() {
        SysLog domain = new SysLog();
        BeanUtils.copyProperties(this, domain);
        return domain;
    }
}
