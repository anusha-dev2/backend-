package com.mediaserver.repository;

import com.mediaserver.model.RootUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RootUserRepository extends MongoRepository<RootUser, String> {
    Optional<RootUser> findByUsername(String username);
    Optional<RootUser> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<RootUser> findByResetPasswordToken(String token);
    Optional<RootUser> findByProviderId(String providerId);
}
