package io.soffa.foundation.spring.data.entities;

import io.soffa.foundation.commons.BeanUtil;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.data.entities.Metric;
import lombok.Data;
import lombok.SneakyThrows;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "sys_metrics")
@Data
public class MetricEntity {

    @Id
    private String id;
    private String name;
    private String data;
    @Column(name = "value_num")
    private Float value;
    private String spanId;
    private String traceId;
    private String userId;
    private String application;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Column(name="date_year")
    private int year;
    @Column(name="date_day")
    private int day;
    @Column(name="date_month")
    private int month;
    private int processingFlag;

    @SneakyThrows
    public static MetricEntity of(Metric model) {
        return BeanUtil.copyProperties(model, new MetricEntity());
    }

    @PrePersist
    public void onPrePersist() {
        if (TextUtil.isEmpty(id)) {
            id = IdGenerator.shortUUID("smt_");
        }
        if (date == null) {
            date = new Date();
        }
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        year = localDate.getYear();
        month = localDate.getMonthValue();
        day = localDate.getDayOfMonth();
    }

    @SneakyThrows
    public Metric toDomain() {
        return BeanUtil.copyProperties(this, new Metric());
    }
}
