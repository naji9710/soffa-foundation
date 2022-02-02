package com.company.app.gateways;

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
