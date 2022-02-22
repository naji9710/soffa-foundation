package io.soffa.foundation.service.pubsub;

import io.soffa.foundation.commons.HttpStatus;
import io.soffa.foundation.commons.JsonUtil;
import io.soffa.foundation.commons.ObjectUtil;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.core.operation.OperationResult;
import io.soffa.foundation.core.pubsub.PubSubClient;
import io.soffa.foundation.core.pubsub.PubSubClientConfig;
import io.soffa.foundation.errors.ForbiddenException;
import io.soffa.foundation.errors.FunctionalException;
import io.soffa.foundation.errors.TechnicalException;
import io.soffa.foundation.errors.UnauthorizedException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractPubSubClient implements PubSubClient {

    protected String broadcasting;
    protected String applicationName;

    public AbstractPubSubClient(String applicationName, PubSubClientConfig config, String broadcasting) {
        this.applicationName = applicationName;
        if (config!=null) {
            this.broadcasting = config.getBroadcasting();
        }
        if (TextUtil.isEmpty(this.broadcasting)) {
            this.broadcasting = broadcasting;
        }
    }

    @Override
    public void setDefaultBroadcast(String value) {
        if (TextUtil.isEmpty(this.broadcasting)) {
            this.broadcasting = value;
        }
    }

    protected String resolveBroadcast(String target) {
        String sub = target;
        boolean isWildcard = "*".equals(sub);
        if (TextUtil.isEmpty(sub) || isWildcard) {
            sub = broadcasting;
        }
        return sub;
    }



    @SuppressWarnings("unchecked")
    @Override
    public final <T> CompletableFuture<T> request(@NonNull String subject, Message message, final Class<T> responseClass) {
        return internalRequest(subject, message).thenApply(data -> unwrapResponse(data, responseClass));
    }

    public abstract CompletableFuture<byte[]> internalRequest(@NonNull String subject, Message message);

    public <T> T unwrapResponse(byte[] data, final Class<T> responseClass) {
        if (data==null) {
            return null;
        }
        OperationResult response = ObjectUtil.deserialize(data, OperationResult.class);
        if (response.isSuccess()) {
            return JsonUtil.deserialize(response.getData(), responseClass);
        } else {
            switch (response.getErrorCode()) {
                case HttpStatus.UNAUTHORIZED:
                    throw new UnauthorizedException(response.getError());
                case HttpStatus.FORBIDDEN:
                    throw new ForbiddenException(response.getError());
                case HttpStatus.BAD_REQUEST:
                    throw new FunctionalException(response.getError());
                default:
                    throw new TechnicalException(response.getError());
            }
        }
    }
}
