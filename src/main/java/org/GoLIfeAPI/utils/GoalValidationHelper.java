package org.GoLIfeAPI.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.GoLIfeAPI.controllers.Persistence.GoalPersistenceController;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Component
public class GoalValidationHelper {

    private final GoalPersistenceController goalPersistenceController;
    private final Validator validator;

    public GoalValidationHelper(GoalPersistenceController goalPersistenceController, Validator validator) {
        this.goalPersistenceController = goalPersistenceController;
        this.validator = validator;
    }

    public Document validateAndGetGoal(String uid, String mid) {
        Document meta = goalPersistenceController.read(mid);
        if (meta == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Meta no encontrada");
        if (!uid.equals(meta.getString("uid")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para acceder a esta meta");
        return meta;
    }

    public <T> void validateDTO(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, violations.iterator().next().getMessage());
        }
    }

    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}