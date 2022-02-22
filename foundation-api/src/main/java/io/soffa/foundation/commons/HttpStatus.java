package io.soffa.foundation.commons;

public interface HttpStatus {
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int BAD_REQUEST = 400;
    int SERVER_ERROR = 500;
    int NO_CONTENT = 204;
    int CONFLICT = 409;
    int NOT_FOUND = 404;
    int NOT_IMLEMENTED = 501;
    int TIMEOUT = 408;
}
