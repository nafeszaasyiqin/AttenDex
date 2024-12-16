package com.example.Attendex.repo;

import com.example.Attendex.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepo extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);  // Find user by username
    long countByRole(String role); // Count users by role
}
