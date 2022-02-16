package io.soffa.foundation.model;

import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.errors.ErrorUtil;
import lombok.Data;
@Data
public class Payload {

    private int errorCode;
    private String message;
    private boolean success;
    private byte[] data;

    public static Payload create(Exception e) {
        Payload response = new Payload();
        response.setSuccess(false);
        response.setErrorCode(ErrorUtil.resolveErrorCode(e));
        response.setMessage(e.getMessage());
        response.setData(null);
        return response;
    }

    public static Payload create(Object payload) {
        Payload response = new Payload();
        response.setSuccess(true);
        response.setData(ObjectUtil.serialize(payload));
        return response;
    }



}
