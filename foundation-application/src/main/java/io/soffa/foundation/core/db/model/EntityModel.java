package io.soffa.foundation.core.db.model;

import java.util.Date;

public interface EntityModel {

    String getId();

    void setId(String value);

    default Date getCreatedAt() {
        return null;
    }

    default void setCreatedAt(Date date){
        // Default implementation
    }
}
