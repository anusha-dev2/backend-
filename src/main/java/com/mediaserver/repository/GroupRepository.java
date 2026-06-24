package com.mediaserver.repository;

import com.mediaserver.model.Group;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    Optional<Group> findByUserIdAndGroupName(String userId, String groupName);
    @Override
    Optional<Group> findById(String id);
    List<Group> findByUserId(String userId);
    Optional<Group> findByDevices(String deviceId);
    void deleteByIdAndUserId(String id, String userId);
    List<Group> findByUserIdAndGroupNameContaining(String userId, String groupName);
    List<Group> findByUserIdAndStatus(String userId, String status);
    List<Group> findByUserIdAndLocation(String userId, String location);
    List<Group> findByUserIdAndGroupNameContainingAndStatus(String userId, String groupName, String status);
    List<Group> findByUserIdAndLocationAndStatus(String userId, String location, String status);

}