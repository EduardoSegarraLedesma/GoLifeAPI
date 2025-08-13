package org.GoLifeAPI.service.implementation;

import org.GoLifeAPI.infrastructure.KeyManagementService;
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
    private final KeyManagementService keyManagementService;
    private final IUserPersistenceController userPersistenceController;

    @Autowired
    public UserService(UserDtoMapper userDtoMapper,
                       UserPatchMapper userPatchMapper,
                       KeyManagementService keyManagementService,
                       IUserPersistenceController userPersistenceController) {
        this.userDtoMapper = userDtoMapper;
        this.userPatchMapper = userPatchMapper;
        this.keyManagementService = keyManagementService;
        this.userPersistenceController = userPersistenceController;
    }

    @Override
    public ResponseUserDTO createUser(CreateUserDTO dto, String uid) {
        String signedUid = keyManagementService.sign(uid);
        User newUser = userDtoMapper.mapCreateUserDtoToUser(dto, signedUid);
        return userDtoMapper.mapUserToResponseUserDTO(
                userPersistenceController.create(newUser, uid));
    }

    @Override
    public ResponseUserDTO getUser(String uid) {
        String signedUid = keyManagementService.sign(uid);
        User user = userPersistenceController.read(signedUid);
        if (user == null) throw new NotFoundException("No se ha encontrado al usuario");
        else return userDtoMapper.mapUserToResponseUserDTO(user);
    }

    @Override
    public ResponseUserDTO updateUser(PatchUserDTO dto, String uid) {
        String signedUid = keyManagementService.sign(uid);
        return userDtoMapper.mapUserToResponseUserDTO(
                userPersistenceController.update(
                        userPatchMapper.mapPatchUserDtoToDoc(dto),
                        signedUid));
    }

    @Override
    public void deleteUser(String uid) {
        String signedUid = keyManagementService.sign(uid);
        userPersistenceController.delete(signedUid, uid);
    }
}