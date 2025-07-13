package com.hsf.hsfproject.service.user;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.dtos.request.LoginRequest;
import com.hsf.hsfproject.dtos.response.LoginResponse;
import com.hsf.hsfproject.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface IUserService {
    User createUser(CreateUserDTO user);
    void updateUser(Long userId, String username, String password);
    void updateUserPassword(String username, String newPassword);
    void deleteUser(UUID id);
    User getUserByName(String name);
    Page<User> getUsers(Pageable pageable);
    User findByUsername(String username);
//    LoginResponse login(LoginRequest loginRequest);
    long count();
    void updateUserRole(UUID userId, String roleName);
}
