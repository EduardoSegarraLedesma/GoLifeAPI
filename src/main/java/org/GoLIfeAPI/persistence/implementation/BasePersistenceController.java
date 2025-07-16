package org.GoLIfeAPI.persistence.implementation;

import org.GoLIfeAPI.persistence.implementation.transaction.TransactionRunner;

public abstract class BasePersistenceController {

    protected final TransactionRunner transactionRunner;

    public BasePersistenceController(TransactionRunner transactionRunner) {
        this.transactionRunner = transactionRunner;
    }

}