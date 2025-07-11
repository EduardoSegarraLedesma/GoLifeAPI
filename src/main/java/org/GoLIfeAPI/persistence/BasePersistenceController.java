package org.GoLIfeAPI.persistence;

import org.GoLIfeAPI.infrastructure.MongoService;

public abstract class BasePersistenceController {

    protected final MongoService mongoService;

    public BasePersistenceController(MongoService mongoService) {
        this.mongoService = mongoService;
    }
}