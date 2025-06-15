package org.GoLIfeAPI.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.GoLIfeAPI.controllers.PersistenceController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/metas")
public class GoalsController {

    @GetMapping
    public List<Document> ConnectionTest() {
        MongoCollection<Document> collection = PersistenceController.getInstance().getCollection("Users");
        FindIterable<Document> documents = collection.find();

        List<Document> results = new ArrayList<>();
        for (Document doc : documents) {
            results.add(doc);
        }

        return results;
    }
}