package com.mediaserver.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.mediaserver.model.Subscription;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {

    @Query("{ 'userId' : ?0, 'status' : { $regex: '^ACTIVE$', $options: 'i' } }")
    Optional<Subscription> findByUserIdAndStatus(String userId, String status);

    @Query("{ 'userId' : ?0, 'status' : { $regex: '^ACTIVE$', $options: 'i' } }")
    List<Subscription> findAllActiveByUserId(String userId);

    Optional<Subscription> findByUserId(String userId);

    @Query("{ 'SubscriptionId' : ?0 }")
    Subscription findBySubscriptionId(String SubscriptionId);

    @Query("{ 'userId' : ?0 }")
    List<Subscription> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("{ 'userId' : ?0, 'status' : ?1 }")
    List<Subscription> findByUserIdAndStatusList(String userId, String status);

    @Query("{ 'userId' : ?0, 'currentPeriodEnd' : { $gte : ?1, $lte : ?2 } }")
    List<Subscription> findByUserIdAndPeriodBetween(String userId, LocalDateTime start, LocalDateTime end);

    default Optional<Subscription> findByHexId(String hexId) {
        return findById(hexId);
    }
}
