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
    // void updateUser(Long userId, String username, String password); // Đã thay bằng UUID
    // void deleteUser(Long userId); // Đã thay bằng UUID
    User getUserByName(String name);
    Page<User> getUsers(Pageable pageable);
    User findByUsername(String username);
    // Thêm các hàm cho admin
    User getUserById(UUID id);
    void updateUser(UUID id, String username, String email, String phoneNumber);
    void deleteUser(UUID id);
//    LoginResponse login(LoginRequest loginRequest);
}
