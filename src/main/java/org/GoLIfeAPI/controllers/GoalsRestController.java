package org.GoLIfeAPI.controllers;

import org.GoLIfeAPI.services.FirebaseService;
import org.GoLIfeAPI.services.PersistenceService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metas")
public class GoalsRestController extends BaseRestController {

    public GoalsRestController(FirebaseService firebaseService, PersistenceService persistenceService) {
        super(firebaseService, persistenceService);
    }

}