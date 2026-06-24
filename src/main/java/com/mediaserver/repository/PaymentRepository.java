// PaymentRepository.java
package com.mediaserver.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mediaserver.model.Payment;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, ObjectId> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Payment> findBySubscriptionIdOrderByCreatedAtDesc(String subscriptionId);
}
