package org.GoLIfeAPI.handler;

import com.mongodb.DuplicateKeyException;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.ForbiddenResourceException;
import org.GoLIfeAPI.exception.NotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 400: Validation errors on @Valid
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Algún campo o campos no son válidos");
        return ResponseEntity.badRequest().body(msg);
    }

    // 400: Malformed or unreadable JSON
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String msg = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Cuerpo de la petición malformado o inválido";
        return ResponseEntity.badRequest().body(msg);
    }

    // 400: Missing request parameter
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        String msg = ex.getParameterName() != null && !ex.getParameterName().isBlank()
                ? "Falta el parámetro requerido: " + ex.getParameterName() : "Falta un parámetro requerido";
        return ResponseEntity.badRequest().body(msg);
    }

    // 400: Invalid parameter or path variable
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex) {
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }

    // 403: Forbidden resource
    @ExceptionHandler(ForbiddenResourceException.class)
    public ResponseEntity<Object> handleForbidden(ForbiddenResourceException ex) {
        String msg = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Acceso denegado";
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
    }

    // 404: Resource not found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        String msg = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Recurso no encontrado";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
    }

    // 409: Conflict
    @ExceptionHandler({DuplicateKeyException.class, ConflictException.class})
    public ResponseEntity<Object> handleConflict(Exception ex) {
        String msg = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Conflicto en la petición";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
    }

    // 500: Any other uncaught exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        String msg = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Error interno, intentelo de nuevo mas tarde.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
    }
}