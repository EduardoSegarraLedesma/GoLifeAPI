package org.GoLifeAPI.service.interfaces;

import org.GoLifeAPI.dto.user.CreateUserDTO;
import org.GoLifeAPI.dto.user.PatchUserDTO;
import org.GoLifeAPI.dto.user.ResponseUserDTO;

public interface IUserService {

    ResponseUserDTO createUser(CreateUserDTO dto, String uid);

    ResponseUserDTO getUser(String uid);

    ResponseUserDTO updateUser(PatchUserDTO dto, String uid);

    void deleteUser(String uid);
}