package com.schoolmanagement.app_admin_Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.app_admin_Entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameOrEmail(String username, String email);
    
    Optional<User> findByEmailOrPhone(String email, String phone);

    Optional<User> findByToken(String token);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
