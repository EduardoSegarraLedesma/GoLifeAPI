package org.GoLIfeAPI.web;

import jakarta.validation.Valid;
import org.GoLIfeAPI.dto.user.CreateUserDTO;
import org.GoLIfeAPI.dto.user.PatchUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.service.interfaces.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UserRestController {

    private final IUserService userService;

    public UserRestController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ResponseUserDTO> postUser(@AuthenticationPrincipal String uid,
                                                    @Valid @RequestBody CreateUserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDTO, uid));
    }

    @GetMapping
    public ResponseEntity<ResponseUserDTO> getUser(@AuthenticationPrincipal String uid) {
        return ResponseEntity.ok(userService.getUser(uid));
    }

    @PatchMapping
    public ResponseEntity<ResponseUserDTO> patchUser(@AuthenticationPrincipal String uid,
                                                     @Valid @RequestBody PatchUserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(userDTO, uid));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal String uid) {
        userService.deleteUser(uid);
        return ResponseEntity.ok("Usuario eliminado exitosamente");
    }
}