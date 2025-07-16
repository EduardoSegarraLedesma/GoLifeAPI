package org.GoLIfeAPI.persistence.implementation;

import com.mongodb.MongoWriteException;
import com.mongodb.client.result.DeleteResult;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.infrastructure.FirebaseService;
import org.GoLIfeAPI.mapper.persistence.UserDocMapper;
import org.GoLIfeAPI.model.user.User;
import org.GoLIfeAPI.persistence.implementation.dao.GoalDAO;
import org.GoLIfeAPI.persistence.implementation.dao.UserDAO;
import org.GoLIfeAPI.persistence.implementation.transaction.TransactionRunner;
import org.GoLIfeAPI.persistence.interfaces.IUserPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserPersistenceController extends BasePersistenceController implements IUserPersistenceController {

    private final UserDocMapper userDocMapper;
    private final FirebaseService firebaseService;
    private final UserDAO userDAO;
    private final GoalDAO goalDAO;

    @Autowired
    public UserPersistenceController(TransactionRunner transactionRunner,
                                     UserDocMapper userDocMapper,
                                     FirebaseService firebaseService,
                                     UserDAO userDAO, GoalDAO goalDAO) {
        super(transactionRunner);
        this.userDocMapper = userDocMapper;
        this.firebaseService = firebaseService;
        this.userDAO = userDAO;
        this.goalDAO = goalDAO;
    }

    @Override
    public User create(User user, String uid) {
        try {
            return transactionRunner.run(session -> {
                Document userDoc = userDocMapper.mapUsertoDoc(user);
                String id = userDAO.insertDoc(session, userDoc);
                if (id == null || id.isBlank()) throw new RuntimeException();
                Document createdUser = userDAO.findDocById(session, id);
                if (createdUser == null) throw new RuntimeException();
                return userDocMapper.mapDocToUser(createdUser);
            });
        } catch (MongoWriteException e) {
            throw new ConflictException("El usuario ya existe");
        } catch (RuntimeException e) {
            firebaseService.deleteFirebaseUser(uid);
            throw new RuntimeException("Error interno al crear el usuario", e);
        }
    }

    @Override
    public User read(String uid) {
        try {
            Document userDoc = userDAO.findUserByUid(uid);
            if (userDoc == null) throw new NotFoundException("No se ha encontrado al usuario");
            return userDocMapper.mapDocToUser(userDoc);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al leer el usuario", e);
        }
    }

    @Override
    public User update(Document update, String uid) {
        try {
            return transactionRunner.run(session -> {
                Document userDoc = userDAO.updateUserByUid(session, uid, update);
                if (userDoc == null) throw new NotFoundException("Usuario no encontrado");
                return userDocMapper.mapDocToUser(userDoc);
            });
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al editar el usuario", e);
        }
    }

    @Override
    public void delete(String uid) {
        try {
            transactionRunner.run(session -> {
                DeleteResult deleteUser = userDAO.deleteUserByUid(session, uid);
                if (!deleteUser.wasAcknowledged()) throw new RuntimeException();
                if (deleteUser.getDeletedCount() == 0) throw new NotFoundException("Usuario no encontrado");
                DeleteResult deleteGoals = goalDAO.deleteManyGoalsByUid(session, uid);
                if (!deleteGoals.wasAcknowledged()) throw new RuntimeException();
                if (!firebaseService.deleteFirebaseUser(uid)) throw new RuntimeException();
                return null;
            });
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al borrar el usuario", e);
        }
    }
}