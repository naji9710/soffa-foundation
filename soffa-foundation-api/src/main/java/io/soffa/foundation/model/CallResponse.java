package io.soffa.foundation.model;

import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.errors.ErrorUtil;
import lombok.Data;

@Data
public class CallResponse {

    private Integer errorCode;
    private String error;
    private byte[] data;

    public boolean hasError() {
        return TextUtil.isNotEmpty(error);
    }

    public boolean isSuccess() {
        return !hasError();
    }

    public static CallResponse error(Exception e) {
        CallResponse response = new CallResponse();
        response.setErrorCode(ErrorUtil.resolveErrorCode(e));
        response.setError(e.getMessage());
        response.setData(null);
        return response;
    }

    public static CallResponse create(Object payload) {
        CallResponse response = new CallResponse();
        response.setData(ObjectUtil.serialize(payload));
        return response;
    }


}
