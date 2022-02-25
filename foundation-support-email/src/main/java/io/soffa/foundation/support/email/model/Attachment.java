package io.soffa.foundation.support.email.model;

import lombok.Data;

@Data
public class Attachment {

    private String id;
    private String url;
    private String content;
    private String name;
    private String description;
    private String contentType;

}
