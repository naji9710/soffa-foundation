package io.soffa.foundation.app.gateway;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "messages")
public class MessageEntity {

    @Id
    private String id;

}
