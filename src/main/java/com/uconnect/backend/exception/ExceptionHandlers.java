package com.uconnect.backend.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ExceptionHandlers {

    private final ObjectMapper mapper;

    @Autowired
    public ExceptionHandlers(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException e) throws JsonProcessingException {
        Map<String, String> ret = new HashMap<>();
        List<ObjectError> errors = e.getBindingResult().getAllErrors();

        for (ObjectError error : errors) {
            ret.put(((FieldError) error).getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(mapper.writeValueAsString(ret));
    }

    @ExceptionHandler(UnauthorizedUserRequestException.class)
    public ResponseEntity<String> handleUnauthorizedRequestExceptions(UnauthorizedUserRequestException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                "You are not authorized to make that request. We've got our eyes on you!");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationExceptions(AuthenticationException e) {
        if (e instanceof OAuth2AuthenticationException) {
            log.info("A failed OAuth authentication attempt occurred: ", e);
        } else {
            log.info("A failed authentication occurred: ", e);
        }
        // purposely leave error message vague to prevent user information leaks
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                "Invalid credentials / Account disabled / Account locked");
    }

    @ExceptionHandler(UnknownOAuthRegistrationException.class)
    public ResponseEntity<String> handleUnknownOAuthRegistrationExceptions(UnknownOAuthRegistrationException e) {
        log.info("An unknown OAuth registration was requested: ", e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                "Provided registration ID is unknown or not yet supported");
    }

    @ExceptionHandler(UnmatchedUserCreationTypeException.class)
    public ResponseEntity<String> handleUnmatchedUserCreationTypeExceptions(UnmatchedUserCreationTypeException e) {
        String unmatchedCreationTypeName = e.getUnmatchedCreationType() == null ? "Unknown" : e.getUnmatchedCreationType().name();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                String.format("Requested user was not created through creation type: %s", unmatchedCreationTypeName));
    }

    @ExceptionHandler({UsernameNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<String> handleUsernameNotFoundExceptions(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "Requested user either does not exist or is not available at this time");
    }

    @ExceptionHandler(DisallowedEmailDomainException.class)
    public ResponseEntity<String> handleDisallowedEmailDomainExceptions(DisallowedEmailDomainException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                String.format("%s: %s", e.getMessage(), e.getEmailAddress()));
    }

    @ExceptionHandler(EventBoardEventNotFoundException.class)
    public ResponseEntity<String> handleEventBoardEventNotFoundException(EventBoardEventNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "Event does not exist");
    }

    @ExceptionHandler(EventBoardEntityNotFoundException.class)
    public ResponseEntity<String> handleEventBoardEntityNotFoundException(EventBoardEntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "Request event board entity (event/comment) does not exist");
    }
}
