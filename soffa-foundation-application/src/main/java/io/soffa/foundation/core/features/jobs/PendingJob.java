package io.soffa.foundation.core.features.jobs;

import io.soffa.foundation.core.db.model.EntityModel;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingJob implements EntityModel {

    private String id;
    private String operation;
    private String subject;
    private String data;
    private Date createdAt;

}
