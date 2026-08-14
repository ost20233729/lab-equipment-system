package com.example.labdesign.repository;

import com.example.labdesign.entity.AppUser;
import com.example.labdesign.enums.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    List<AppUser> findByRole(UserRole role);
}
