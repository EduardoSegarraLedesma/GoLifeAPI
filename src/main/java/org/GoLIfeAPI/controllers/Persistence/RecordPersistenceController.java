package org.GoLIfeAPI.controllers.Persistence;

import com.mongodb.client.ClientSession;
import org.GoLIfeAPI.Models.Records.Record;
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
                Document doc = mongoService.findOneById(mid, GOAL_COLLECTION_NAME);
                session.commitTransaction();
                session.close();
                return doc;
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            return null;
        }
    }

    public Boolean delete(String mid, String rid) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            if (mongoService.deleteOneEmbeddedDocByParentId(session, mid, GOAL_COLLECTION_NAME,
                    RECORD_LIST_NAME, rid)) {
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