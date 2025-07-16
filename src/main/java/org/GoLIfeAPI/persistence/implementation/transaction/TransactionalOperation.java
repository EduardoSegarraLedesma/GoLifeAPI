
package org.GoLIfeAPI.persistence.implementation.transaction;

import com.mongodb.client.ClientSession;

@FunctionalInterface
public interface TransactionalOperation<T> {

    T apply(ClientSession session);

}