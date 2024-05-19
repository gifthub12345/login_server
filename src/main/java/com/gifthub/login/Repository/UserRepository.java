package com.gifthub.login.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gifthub.login.Entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByIdentifier(String identifier);
}
