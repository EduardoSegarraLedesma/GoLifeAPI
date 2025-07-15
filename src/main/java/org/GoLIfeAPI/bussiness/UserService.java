package org.GoLIfeAPI.bussiness;

import org.GoLIfeAPI.dto.user.CreateUserDTO;
import org.GoLIfeAPI.dto.user.PatchUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.mapper.bussiness.UserDtoMapper;
import org.GoLIfeAPI.mapper.bussiness.UserPatchMapper;
import org.GoLIfeAPI.model.user.User;
import org.GoLIfeAPI.persistence.UserPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDtoMapper userDtoMapper;
    private final UserPatchMapper userPatchMapper;
    private final UserPersistenceController userPersistenceController;

    @Autowired
    public UserService(UserDtoMapper userDtoMapper,
                       UserPatchMapper userPatchMapper,
                       UserPersistenceController userPersistenceController) {
        this.userDtoMapper = userDtoMapper;
        this.userPatchMapper = userPatchMapper;
        this.userPersistenceController = userPersistenceController;
    }

    public ResponseUserDTO createUser(CreateUserDTO dto, String uid) {
        User newUser = userDtoMapper.mapCreateUserDtoToUser(dto, uid);
        return userDtoMapper.mapUserToResponseUserDTO(
                userPersistenceController.create(newUser, uid));
    }

    public ResponseUserDTO getUser(String uid) {
        User user = userPersistenceController.read(uid);
        if (user == null) throw new NotFoundException("No se ha encontrado al usuario");
        else return userDtoMapper.mapUserToResponseUserDTO(user);
    }

    public ResponseUserDTO updateUser(PatchUserDTO dto, String uid) {
        Document updateUserDoc = userPatchMapper.mapPatchUserDtoToDoc(dto);
        return userDtoMapper.mapUserToResponseUserDTO(
                        userPersistenceController.update(updateUserDoc, uid));
    }

    public void deleteUser(String uid) {
        userPersistenceController.delete(uid);
    }
}