package io.soffa.foundation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.errors.ErrorUtil;
import lombok.Data;

@Data
public class OperationResult {

    private Integer errorCode;
    private String error;
    private byte[] data;

    @JsonIgnore
    public boolean hasError() {
        return TextUtil.isNotEmpty(error);
    }

    public boolean isSuccess() {
        return !hasError();
    }

    public static OperationResult create(Object payload, Exception e) {
        OperationResult response = new OperationResult();
        if (e!=null) {
            response.setErrorCode(ErrorUtil.resolveErrorCode(e));
            response.setError(e.getMessage());
        }
        response.setData(ObjectUtil.serialize(payload));
        return response;
    }

}
