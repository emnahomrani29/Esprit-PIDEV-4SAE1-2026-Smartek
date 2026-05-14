package com.smartek.authservice.repository;

import com.smartek.authservice.entity.User;
import com.smartek.authservice.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByEmail(String email);
    
    List<User> findByRole(RoleType role);
    
    Long countByRole(RoleType role);
}
