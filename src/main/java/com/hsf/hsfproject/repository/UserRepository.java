package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);
    Optional<User> findById(UUID id);
    void deleteById(UUID id);

}

