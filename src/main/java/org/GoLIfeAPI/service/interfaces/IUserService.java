package org.GoLIfeAPI.service.interfaces;

import org.GoLIfeAPI.dto.user.CreateUserDTO;
import org.GoLIfeAPI.dto.user.PatchUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;

public interface IUserService {

    ResponseUserDTO createUser(CreateUserDTO dto, String uid);

    ResponseUserDTO getUser(String uid);

    ResponseUserDTO updateUser(PatchUserDTO dto, String uid);

    void deleteUser(String uid);
}