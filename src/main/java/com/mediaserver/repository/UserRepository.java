// Updated UserRepository.java to add email methods
package com.mediaserver.repository;

import com.mediaserver.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    //Optional<User> findByUsername(String username);
    //Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);
   // Optional<User> findByStripeCustomerId(String stripeCustomerId); // Add this line
    Optional<User> findByProviderId(String providerId);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByStripeCustomerId(String stripeCustomerId);

    List<User> findByNameContainingIgnoreCase(String name);

}
