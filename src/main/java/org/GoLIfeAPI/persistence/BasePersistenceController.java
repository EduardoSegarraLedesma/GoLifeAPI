package org.GoLIfeAPI.persistence;

import org.GoLIfeAPI.persistence.transaction.TransactionRunner;

public abstract class BasePersistenceController {

    protected final TransactionRunner transactionRunner;

    public BasePersistenceController(TransactionRunner transactionRunner) {
        this.transactionRunner = transactionRunner;
    }

}