package org.GoLifeAPI.persistence.interfaces;

import org.GoLifeAPI.model.user.User;
import org.bson.Document;

public interface IUserPersistenceController {

    User create(User user, String uid);

    User read(String uid);

    User update(Document update, String uid);

    void delete(String uid);
}
