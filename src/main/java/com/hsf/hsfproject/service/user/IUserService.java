package com.hsf.hsfproject.service.user;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    void createUser(CreateUserDTO user);
    void updateUser(Long userId, String username, String password);
    void deleteUser(Long userId);
    User getUserByName(String name);
    Page<User> getUsers(Pageable pageable);
}
