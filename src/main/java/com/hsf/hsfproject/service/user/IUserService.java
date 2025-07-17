package com.hsf.hsfproject.service.user;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.model.Role;
import com.hsf.hsfproject.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    User createUser(CreateUserDTO user);
    void updateUser(UUID userId, String username, String password);
    void deleteUser(UUID userId);
    User getUserByName(String name);
    Page<User> getUsers(Pageable pageable);
    User findByUsername(String username);
    User getUserById(UUID id);
    List<Role> getAllRoles();


    void updateUser(UUID id, String username, String email, String phoneNumber, String password, User currentUser);
//    LoginResponse login(LoginRequest loginRequest);
}
