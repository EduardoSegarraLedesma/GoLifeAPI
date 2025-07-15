
package org.GoLIfeAPI.persistence.transaction;

import com.mongodb.client.ClientSession;

@FunctionalInterface
public interface TransactionalOperation<T> {

    T apply(ClientSession session);

}