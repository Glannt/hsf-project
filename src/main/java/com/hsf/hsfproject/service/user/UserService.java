package com.hsf.hsfproject.service.user;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.repository.RoleRepository;
import com.hsf.hsfproject.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j(topic = "UserService")
@AllArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void createUser(CreateUserDTO userDTO) {
        try {
            User user = userRepository.findByUsername(userDTO.getUsername());
            if (user != null) {
                log.warn("User with username {} already exists", userDTO.getUsername());
                return;
            }

            User newUser = User.builder()
                    .username(userDTO.getUsername())
                    .password(userDTO.getPassword())
                    .email(userDTO.getEmail())
                    .phoneNumber(userDTO.getPhoneNumber())
                    .role(roleRepository.findByName("USER"))
                    .build();

            userRepository.save(newUser);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        @Override
        public void updateUser (Long userId, String username, String password){

        }

        @Override
        public void deleteUser (Long userId){

        }

        @Override
        public User getUserByName (String name){
            return null;
        }

        @Override
        public Page<User> getUsers (Pageable pageable){
            return null;
        }
    }
