package org.GoLIfeAPI.persistence;

import com.mongodb.client.ClientSession;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.infrastructure.MongoService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RecordPersistenceController extends BasePersistenceController {

    @Autowired
    public RecordPersistenceController(MongoService mongoService) {
        super(mongoService);
    }

    public Document create(Document record, Document goalStatsUpdate, String mid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            Document goalDoc = mongoService.insertEmbeddedDocInListByParentId(session, mid, GOAL_COLLECTION_NAME,
                    RECORD_LIST_NAME, record);
            if (goalDoc == null) throw new NotFoundException("");
            if (goalStatsUpdate != null && !goalStatsUpdate.isEmpty()) {
                goalDoc = mongoService.updateSetEmbeddedDocByParentId(session, mid, GOAL_COLLECTION_NAME, STATS_NAME, goalStatsUpdate);
                if (goalDoc == null) throw new NotFoundException("");
            }
            session.commitTransaction();
            session.close();
            return goalDoc;
        } catch (NotFoundException e) {
            session.abortTransaction();
            session.close();
            throw new NotFoundException("Meta del registro no encontrada");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al crear el registro", e);
        }
    }

    public Document delete(String mid, String date) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            Document goalDoc = mongoService.removeEmbeddedDocInListByParentIdSonKey(session,
                    mid, GOAL_COLLECTION_NAME, RECORD_LIST_NAME, "fecha", date);
            if (goalDoc == null) throw new NotFoundException("");
            session.commitTransaction();
            session.close();
            return goalDoc;
        } catch (NotFoundException e) {
            session.abortTransaction();
            session.close();
            throw new NotFoundException("Registro no encontrado");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al borrar el registro", e);
        }
    }
}