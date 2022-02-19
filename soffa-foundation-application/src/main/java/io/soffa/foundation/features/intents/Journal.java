package io.soffa.foundation.features.intents;

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
public class Journal implements EntityModel {

    @Id
    private String id;
    private String event;
    private String subject;
    private String data;
    private String status;
    private Date createdAt;

}
