package com.sujalsamai.auth_service;

import com.sujalsamai.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
public interface UserRepository extends JpaRepository<User, UUID>
{
   Optional<User> findByEmail(String email);
}
