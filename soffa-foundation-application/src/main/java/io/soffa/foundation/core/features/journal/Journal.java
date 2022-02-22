package io.soffa.foundation.core.features.journal;

import io.soffa.foundation.core.db.model.EntityModel;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Journal implements EntityModel {

    private String id;
    private String event;
    private String subject;
    private String data;
    private String status;
    private Date createdAt;

}
