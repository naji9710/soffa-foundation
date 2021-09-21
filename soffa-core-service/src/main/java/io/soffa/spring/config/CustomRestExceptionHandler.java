package io.soffa.spring.config;

import io.soffa.commons.exceptions.*;
import io.soffa.commons.logging.Logger;
import lombok.AllArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@AllArgsConstructor
class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    private Environment environment;
    private static final Logger logger = Logger.create(CustomRestExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> handleGlobalErrors(Throwable ex, WebRequest request) {
        boolean isProduction = environment.acceptsProfiles(Profiles.of("prod", "production"));
        Throwable error = Errors.unwrap(ex);
        HttpStatus status = deriverStatus(error);
        String message = Errors.loookupOriginalMessage(error);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("kind", error.getClass().getSimpleName());
        body.put("status", status.value());
        body.put("message", message);
        body.put("prod", isProduction);

        if (!isProduction && status != HttpStatus.UNAUTHORIZED && status != HttpStatus.FORBIDDEN) {
            logger.error(error);
            body.put("trace", Errors.getStacktrace(error).split("\n"));
        }

        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus deriverStatus(Throwable exception) {
        if (exception instanceof ValidationException) {
            return HttpStatus.BAD_REQUEST;
        } else if (exception instanceof ConflictException) {
            return HttpStatus.CONFLICT;
        } else if (exception instanceof ResponseStatusException) {
            return ((ResponseStatusException) exception).getStatus();
        } else if (exception instanceof ForbiddenException) {
            return HttpStatus.FORBIDDEN;
        } else if (exception instanceof UnauthorizedException || exception instanceof AccessDeniedException) {
            return HttpStatus.UNAUTHORIZED;
        } else if (exception instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }


}
