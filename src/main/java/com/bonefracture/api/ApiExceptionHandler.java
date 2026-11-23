package com.bonefracture.api;

import com.bonefracture.auth.EmailAlreadyUsedException;
import com.bonefracture.auth.InvalidCredentialsException;
import com.bonefracture.booking.AppointmentNotFoundException;
import com.bonefracture.booking.NotAllowedException;
import com.bonefracture.booking.NotificationNotFoundException;
import com.bonefracture.security.AccessDeniedException;
import com.bonefracture.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns the exceptions of the services into HTTP responses, in one place for all controllers.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({AppointmentNotFoundException.class, NotificationNotFoundException.class,
            UserNotFoundException.class})
    public ProblemDetail notFound(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({NotAllowedException.class, AccessDeniedException.class})
    public ProblemDetail notAllowed(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    /** Thrown by the STATE classes when an action is not allowed in the current state. */
    @ExceptionHandler({IllegalStateException.class, EmailAlreadyUsedException.class})
    public ProblemDetail conflict(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badRequest(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail unauthorized(InvalidCredentialsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
}
