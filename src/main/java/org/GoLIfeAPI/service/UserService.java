package org.GoLIfeAPI.service;

import org.GoLIfeAPI.dto.user.CreateUserDTO;
import org.GoLIfeAPI.dto.user.PatchUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.mapper.UserMapper;
import org.GoLIfeAPI.model.user.User;
import org.GoLIfeAPI.persistence.UserPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserPersistenceController userPersistenceController;

    @Autowired
    public UserService(UserMapper userMapper,
                       UserPersistenceController userPersistenceController) {
        this.userMapper = userMapper;
        this.userPersistenceController = userPersistenceController;
    }

    public ResponseUserDTO createUser(CreateUserDTO dto, String uid) {
        User newUser = userMapper.mapCreateUserDtoToUser(dto, uid);
        Document newUserDoc = userMapper.mapUsertoUserDoc(newUser);
        return userMapper.
                mapUserDocToResponseUserDTO(
                        userPersistenceController.create(newUserDoc, uid));
    }

    public ResponseUserDTO getUser(String uid) {
        Document userDoc = userPersistenceController.read(uid);
        if (userDoc == null) throw new NotFoundException("No se ha encontrado al usuario");
        else return userMapper.mapUserDocToResponseUserDTO(userDoc);
    }

    public ResponseUserDTO updateUser(PatchUserDTO dto, String uid) {
        Document updateUserDoc = userMapper.mapPatchUserDtoToDoc(dto);
        return userMapper.
                mapUserDocToResponseUserDTO(
                        userPersistenceController.update(updateUserDoc, uid));
    }

    public void deleteUser(String uid) {
        userPersistenceController.delete(uid);
    }
}