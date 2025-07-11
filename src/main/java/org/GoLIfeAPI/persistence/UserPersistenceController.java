package org.GoLIfeAPI.persistence;

import com.mongodb.MongoWriteException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.result.DeleteResult;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.infrastructure.FirebaseService;
import org.GoLIfeAPI.infrastructure.MongoService;
import org.GoLIfeAPI.persistence.dao.GoalDAO;
import org.GoLIfeAPI.persistence.dao.UserDAO;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserPersistenceController extends BasePersistenceController {

    private final FirebaseService firebaseService;
    private final UserDAO userDAO;
    private final GoalDAO goalDAO;

    @Autowired
    public UserPersistenceController(MongoService mongoService,
                                     FirebaseService firebaseService,
                                     UserDAO userDAO, GoalDAO goalDAO) {
        super(mongoService);
        this.firebaseService = firebaseService;
        this.userDAO = userDAO;
        this.goalDAO = goalDAO;
    }

    public Document create(Document user, String uid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            String objectId = userDAO.insertDoc(session, user);
            System.out.println("id:" + objectId);
            if (objectId != null && !objectId.isBlank()) {
                session.commitTransaction();
                session.close();
                return userDAO.findDocById(objectId);
            } else throw new Exception();
        } catch (MongoWriteException e) {
            session.abortTransaction();
            session.close();
            throw new ConflictException("El usuario ya existe");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            firebaseService.deleteFirebaseUser(uid);
            throw new RuntimeException("Error interno al crear el usuario", e);
        }
    }

    public Document read(String uid) {
        try {
            Document userDoc = userDAO.findUserByUid(uid);
            if (userDoc == null) throw new NotFoundException("");
            return userDoc;
        } catch (NotFoundException e) {
            throw new NotFoundException("No se ha encontrado al usuario");
        } catch (Exception e) {
            throw new RuntimeException("Error interno al leer el usuario", e);
        }
    }

    public Document update(Document update, String uid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            Document userDoc = userDAO.updateUserByUid(session, uid, update);
            if (userDoc == null) throw new NotFoundException("");
            session.commitTransaction();
            session.close();
            return userDoc;
        } catch (NotFoundException e) {
            session.abortTransaction();
            session.close();
            throw new NotFoundException("Usuario no encontrado");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al editar el usuario", e);
        }
    }

    public void delete(String uid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            DeleteResult deleteUser = userDAO.deleteUserByUid(session, uid);
            if (!deleteUser.wasAcknowledged()) throw new Exception();
            if (deleteUser.getDeletedCount() == 0) throw new NotFoundException("");
            DeleteResult deleteGoals = goalDAO.deleteManyGoalsByUid(session, uid);
            if (!deleteGoals.wasAcknowledged()) throw new Exception();
            if (firebaseService.deleteFirebaseUser(uid)) {
                session.commitTransaction();
                session.close();
            } else throw new Exception();
        } catch (NotFoundException e) {
            session.abortTransaction();
            session.close();
            throw new NotFoundException("Usuario no encontrado");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al borrar el usuario", e);
        }
    }
}