package org.GoLifeAPI.service.implementation;

import org.GoLifeAPI.service.interfaces.IUserService;
import org.GoLifeAPI.dto.user.CreateUserDTO;
import org.GoLifeAPI.dto.user.PatchUserDTO;
import org.GoLifeAPI.dto.user.ResponseUserDTO;
import org.GoLifeAPI.exception.NotFoundException;
import org.GoLifeAPI.mapper.service.UserDtoMapper;
import org.GoLifeAPI.mapper.service.UserPatchMapper;
import org.GoLifeAPI.model.user.User;
import org.GoLifeAPI.persistence.interfaces.IUserPersistenceController;
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