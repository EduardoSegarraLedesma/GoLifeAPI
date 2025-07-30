package org.GoLifeAPI.persistence.implementation;

import org.GoLifeAPI.persistence.implementation.transaction.TransactionRunner;

public abstract class BasePersistenceController {

    protected final TransactionRunner transactionRunner;

    public BasePersistenceController(TransactionRunner transactionRunner) {
        this.transactionRunner = transactionRunner;
    }

}