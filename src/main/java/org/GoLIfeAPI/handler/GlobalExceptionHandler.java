package org.GoLIfeAPI.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mongodb.DuplicateKeyException;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.ForbiddenResourceException;
import org.GoLIfeAPI.exception.NotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 400: Missing request parameter
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String message = ex.getParameterName() != null && !ex.getParameterName().isBlank()
                ? "Falta el parámetro requerido: " + ex.getParameterName()
                : "Falta un parámetro requerido";
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    // 400: Invalid parameter or path variable
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    // 400: Malformed or unreadable JSON
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String message = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "Cuerpo de la petición malformado o inválido";
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    // 400: Jackson parse errors
    @ExceptionHandler({ JsonParseException.class, JsonMappingException.class })
    public ResponseEntity<Object> handleJacksonExceptions(
            JsonProcessingException ex,
            WebRequest request) {
        String details = ex.getOriginalMessage();
        String message = "Error al parsear JSON: " + details;
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    // Handles ResponseStatusException thrown from controllers
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatus(ResponseStatusException ex, WebRequest request) {
        String error = ex.getStatusCode().toString();
        String message = ex.getReason();
        return buildResponse(ex.getStatusCode(), error, message, request);
    }

    // 400: Validation errors on @Valid
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Algún campo o campos no son válidos");
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    // 403: Forbidden resource
    @ExceptionHandler(ForbiddenResourceException.class)
    public ResponseEntity<Object> handleForbidden(ForbiddenResourceException ex, WebRequest request) {
        String message = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Acceso denegado";
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", message, request);
    }

    // 404: Resource not found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex, WebRequest request) {
        String message = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Recurso no encontrado";
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", message, request);
    }

    // 409: Conflict
    @ExceptionHandler({ DuplicateKeyException.class, ConflictException.class })
    public ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
        String message = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Conflicto en la petición";
        return buildResponse(HttpStatus.CONFLICT, "Conflict", message, request);
    }

    // 500: Any other uncaught exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        String message = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Error interno, inténtelo de nuevo más tarde.";
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message, request);
    }


    private ResponseEntity<Object> buildResponse(HttpStatusCode status,
                                                 String error,
                                                 String message,
                                                 WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        String path = request instanceof ServletWebRequest
                ? ((ServletWebRequest) request).getRequest().getRequestURI()
                : null;
        body.put("path", path);

        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}