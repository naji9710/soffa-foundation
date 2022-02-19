package io.soffa.foundation.features.jobs;

import io.soffa.foundation.data.EntityModel;
import lombok.*;

import javax.persistence.Id;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingJob implements EntityModel {

    @Id
    private String id;
    private String operation;
    private String subject;
    private String data;
    private Date createdAt;

}
