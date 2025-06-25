package org.GoLIfeAPI.controllers.Persistence;

import org.GoLIfeAPI.services.MongoService;

public abstract class BasePersistenceController {

    protected static final String USER_COLLECTION_NAME = "Users";
    protected static final String GOAL_COLLECTION_NAME = "Goals";
    protected static final String GOAL_LIST_NAME = "metas";
    protected static final String RECORD_LIST_NAME = "registros";
    protected final MongoService mongoService;

    public BasePersistenceController(MongoService mongoService) {
        this.mongoService = mongoService;
    }

}