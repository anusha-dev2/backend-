// // SubscriptionPlanRepository.java
// package com.mediaserver.repository;

// import java.util.Optional;

// import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.stereotype.Repository;

// import com.mediaserver.model.SubscriptionPlan;

// @Repository
// public interface SubscriptionPlanRepository extends MongoRepository<SubscriptionPlan, String> {
//     Optional<SubscriptionPlan> findByPriceId(String PriceId);
//     Optional<SubscriptionPlan> findByPlanCode(String planCode);

//     default Optional<SubscriptionPlan> findByHexId(String hexId) {
//         return findById(hexId);
//     }
// }

// SubscriptionPlanRepository.java
package com.mediaserver.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.mediaserver.model.SubscriptionPlan;

@Repository
public interface SubscriptionPlanRepository extends MongoRepository<SubscriptionPlan, String> {
    
    // Use @Query to explicitly specify the field name in MongoDB
    @Query("{ 'priceId' : ?0 }")
    Optional<SubscriptionPlan> findByPriceId(String priceId);
    
    Optional<SubscriptionPlan> findByPlanCode(String planCode);

    default Optional<SubscriptionPlan> findByHexId(String hexId) {
        return findById(hexId);
    }
}