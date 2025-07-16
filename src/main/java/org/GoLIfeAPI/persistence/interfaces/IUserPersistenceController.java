package org.GoLIfeAPI.persistence.interfaces;

import org.GoLIfeAPI.model.user.User;
import org.bson.Document;

public interface IUserPersistenceController {

    User create(User user, String uid);

    User read(String uid);

    User update(Document update, String uid);

    void delete(String uid);
}
