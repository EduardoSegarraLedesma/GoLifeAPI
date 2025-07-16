package org.GoLIfeAPI.bussiness.implementation;

import org.GoLIfeAPI.bussiness.interfaces.IUserService;
import org.GoLIfeAPI.dto.user.CreateUserDTO;
import org.GoLIfeAPI.dto.user.PatchUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.mapper.bussiness.UserDtoMapper;
import org.GoLIfeAPI.mapper.bussiness.UserPatchMapper;
import org.GoLIfeAPI.model.user.User;
import org.GoLIfeAPI.persistence.interfaces.IUserPersistenceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private final UserDtoMapper userDtoMapper;
    private final UserPatchMapper userPatchMapper;
    private final IUserPersistenceController userPersistenceController;

    @Autowired
    public UserService(UserDtoMapper userDtoMapper,
                       UserPatchMapper userPatchMapper,
                       IUserPersistenceController userPersistenceController) {
        this.userDtoMapper = userDtoMapper;
        this.userPatchMapper = userPatchMapper;
        this.userPersistenceController = userPersistenceController;
    }

    @Override
    public ResponseUserDTO createUser(CreateUserDTO dto, String uid) {
        User newUser = userDtoMapper.mapCreateUserDtoToUser(dto, uid);
        return userDtoMapper.mapUserToResponseUserDTO(
                userPersistenceController.create(newUser, uid));
    }

    @Override
    public ResponseUserDTO getUser(String uid) {
        User user = userPersistenceController.read(uid);
        if (user == null) throw new NotFoundException("No se ha encontrado al usuario");
        else return userDtoMapper.mapUserToResponseUserDTO(user);
    }

    @Override
    public ResponseUserDTO updateUser(PatchUserDTO dto, String uid) {
        return userDtoMapper.mapUserToResponseUserDTO(
                userPersistenceController.update(
                        userPatchMapper.mapPatchUserDtoToDoc(dto),
                        uid));
    }

    @Override
    public void deleteUser(String uid) {
        userPersistenceController.delete(uid);
    }
}