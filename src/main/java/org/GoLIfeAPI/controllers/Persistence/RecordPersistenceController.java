package org.GoLIfeAPI.controllers.Persistence;

import com.mongodb.client.ClientSession;
import org.GoLIfeAPI.models.Records.Record;
import org.GoLIfeAPI.services.MongoService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecordPersistenceController extends BasePersistenceController {

    @Autowired
    public RecordPersistenceController(MongoService mongoService) {
        super(mongoService);
    }

    public Document create(Record record, String mid) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            if (mongoService.insertOneEmbeddedDocByParentId(session, mid, GOAL_COLLECTION_NAME,
                    RECORD_LIST_NAME, record.toDocument())) {
                session.commitTransaction();
                session.close();
                return mongoService.findOneById(mid, GOAL_COLLECTION_NAME);
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            return null;
        }
    }

    public Boolean delete(String mid, String date) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            if (mongoService.deleteOneEmbeddedDocByParentIdSonKey(session,
                    mid, GOAL_COLLECTION_NAME, RECORD_LIST_NAME, "fecha", date)) {
                session.commitTransaction();
                session.close();
                return true;
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            return false;
        }
    }
}