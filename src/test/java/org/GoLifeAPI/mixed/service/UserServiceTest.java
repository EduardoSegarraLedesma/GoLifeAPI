package org.GoLifeAPI.mixed.service;

import org.GoLifeAPI.dto.user.CreateUserDTO;
import org.GoLifeAPI.dto.user.PatchUserDTO;
import org.GoLifeAPI.dto.user.ResponseUserDTO;
import org.GoLifeAPI.exception.NotFoundException;
import org.GoLifeAPI.infrastructure.KeyManagementService;
import org.GoLifeAPI.mapper.service.GoalDtoMapper;
import org.GoLifeAPI.mapper.service.RecordDtoMapper;
import org.GoLifeAPI.mapper.service.UserDtoMapper;
import org.GoLifeAPI.mapper.service.UserPatchMapper;
import org.GoLifeAPI.model.user.User;
import org.GoLifeAPI.persistence.interfaces.IUserPersistenceController;
import org.GoLifeAPI.service.implementation.UserService;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Spy
    private UserDtoMapper userDtoMapper = new UserDtoMapper(new GoalDtoMapper(new RecordDtoMapper()));
    @Spy
    private UserPatchMapper userPatchMapper = new UserPatchMapper();

    @Mock
    private IUserPersistenceController userPersistenceController;

    @Mock
    private KeyManagementService keyManagementService;

    @InjectMocks
    private UserService userService;

    private String uid;
    private String signedUid;
    private CreateUserDTO createUserDTO;
    private PatchUserDTO patchUserDTO;
    private User user;
    private ResponseUserDTO responseUserDTO;

    @BeforeEach
    void setUp() {
        clearInvocations(userPersistenceController);
        uid = "test-uid";
        signedUid = "signed-" + uid;
        when(keyManagementService.sign(uid)).thenReturn(signedUid);
        createUserDTO = null;
        patchUserDTO = null;
        user = null;
        responseUserDTO = null;
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {
        @Test
        public void CreateUser_whenValidInput_returnsResponseUserDTO() {
            createUserDTO = new CreateUserDTO();
            createUserDTO.setNombre("test-nombre");
            user = new User(signedUid, "", "test-nombre");

            when(userPersistenceController.create(any(User.class), eq(uid)))
                    .thenReturn(user);

            ResponseUserDTO createdUser = userService.createUser(createUserDTO, uid);

            Assertions.assertThat(createdUser).isNotNull();
            Assertions.assertThat(createdUser.getNombre()).isEqualTo("test-nombre");
            Assertions.assertThat(createdUser.getApellidos()).isEqualTo("");

            verify(userPersistenceController).create(any(User.class), eq(uid));
        }
    }

    @Nested
    @DisplayName("getUser")
    class GetUser {
        @Test
        public void getUser_whenUserExists_returnsResponseUserDTO() {
            user = new User(signedUid, "", "test-nombre");
            when(userPersistenceController.read(eq(signedUid)))
                    .thenReturn(user);

            ResponseUserDTO result = userService.getUser(uid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getNombre()).isEqualTo("test-nombre");
            Assertions.assertThat(result.getApellidos()).isEqualTo("");
            verify(userPersistenceController).read(eq(signedUid));
        }

        @Test
        public void getUser_whenUserNotFound_throwsNotFoundException() {
            when(userPersistenceController.read(eq(signedUid)))
                    .thenReturn(null);

            Assertions.assertThatThrownBy(() -> userService.getUser(uid))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No se ha encontrado al usuario");

            verify(userPersistenceController).read(eq(signedUid));
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {
        @Test
        public void updateUser_whenValidInput_returnsResponseUserDTO() {
            patchUserDTO = new PatchUserDTO();
            patchUserDTO.setNombre("nuevo-nombre");
            patchUserDTO.setApellidos("nuevo-apellido");
            user = new User(signedUid, "nuevo-apellido", "nuevo-nombre");

            when(userPersistenceController.update(any(Document.class), eq(signedUid)))
                    .thenReturn(user);

            ResponseUserDTO result = userService.updateUser(patchUserDTO, uid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getNombre()).isEqualTo("nuevo-nombre");
            Assertions.assertThat(result.getApellidos()).isEqualTo("nuevo-apellido");
            verify(userPersistenceController).update(any(Document.class), eq(signedUid));
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class deleteUser {
        @Test
        public void deleteUser_whenValidUid_delegatesToPersistence() {
            userService.deleteUser(uid);
            verify(userPersistenceController).delete(eq(signedUid));
        }
    }
}