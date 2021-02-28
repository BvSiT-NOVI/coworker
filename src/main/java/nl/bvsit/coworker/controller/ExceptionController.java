package nl.bvsit.coworker.controller;

import nl.bvsit.coworker.exceptions.BadRequestException;
import nl.bvsit.coworker.exceptions.ForbiddenException;
import nl.bvsit.coworker.exceptions.RecordNotFoundException;
import nl.bvsit.coworker.exceptions.UserNotFoundException;
import nl.bvsit.coworker.payload.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

@RestController
@ControllerAdvice
public class ExceptionController {

    //See https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleUnexpectedExceptions(Exception exception, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(exception.getMessage()); //debug
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponse> handleAccesDeniedException(AccessDeniedException exc) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(exc.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<MessageResponse> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new MessageResponse("File too large!"));
    }

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<Object> exception(ValidationException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage()); //debug
    }

    @ExceptionHandler(value = RecordNotFoundException.class)
    public ResponseEntity<Object> exception(RecordNotFoundException exception) {
        return ResponseEntity.notFound().build();
    }


    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<Object> exception(BadRequestException exception) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<Object> exception(UserNotFoundException exception) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(value = ForbiddenException.class)
    public ResponseEntity<Object> exception(ForbiddenException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    private String extractBaseMessage(String message){
        if (message.indexOf(';')>-1) message= message.substring(0, message.indexOf(';'));
        return message;
    }



}
