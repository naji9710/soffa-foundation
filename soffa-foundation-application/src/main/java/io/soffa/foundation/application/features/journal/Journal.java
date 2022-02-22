package io.soffa.foundation.application.features.journal;

import io.soffa.foundation.infrastructure.db.model.EntityModel;
import lombok.*;

import javax.persistence.Id;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Journal implements EntityModel {

    @Id
    private String id;
    private String event;
    private String subject;
    private String data;
    private String status;
    private Date createdAt;

}
