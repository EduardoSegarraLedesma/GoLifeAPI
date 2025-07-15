package org.GoLIfeAPI.persistence.transaction;

import com.mongodb.ReadConcern;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import org.GoLIfeAPI.infrastructure.MongoService;
import org.springframework.stereotype.Component;

@Component
public class TransactionRunner {
    private final MongoService mongoService;

    public TransactionRunner(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    public <T> T run(TransactionalOperation<T> op) {
        ClientSession session = mongoService.getStartedSession();
        session.startTransaction(buildTransactionOptions());
        try {
            T result = op.apply(session);
            session.commitTransaction();
            return result;
        } catch (Exception e) {
            session.abortTransaction();
            throw e;
        } finally {
            session.close();
        }
    }

    private TransactionOptions buildTransactionOptions() {
        return TransactionOptions.builder()
                .readConcern(ReadConcern.SNAPSHOT)
                .writeConcern(WriteConcern.MAJORITY)
                .build();
    }
}