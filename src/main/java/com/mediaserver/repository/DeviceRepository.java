// // DeviceRepository.java
// package com.mediaserver.repository;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.mongodb.repository.Aggregation;
// import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.stereotype.Repository;

// import com.mediaserver.dto.MonthlyAggregate;
// import com.mediaserver.dto.WeeklyAggregate;
// import com.mediaserver.model.Device;

// @Repository
// public interface DeviceRepository extends MongoRepository<Device, String> {
//     Optional<Device> findByMacAddress(String macAddress);
//     List<Device> findByUserId(String userId);
//     boolean existsByMacAddress(String macAddress);
//     boolean existsById(String id);
//     //List<Device> findByStatus(String status);
//     List<Device> findByUserIdAndStatus(String userId, String status);
//     //Optional<Device> findById(String id);
//     // added 11/6
//     Optional<Device> findById(String deviceId);
//     List<Device> findAllById(List<String> ids);
//     List<Device> findByGroupId(String groupId);


//     // add the repo for filter part 18.6.25
//     List<Device> findByDeviceName(String deviceName);
//     List<Device> findByDeviceNameAndEnabled(String deviceName, Boolean enabled);
//     List<Device> findByStatus(String status);
//     List<Device> findByDeviceNameAndStatusAndEnabled(String deviceName, String status, boolean enabled);
//     List<Device> findByStatusAndLocation(String status, String location);
//     List<Device> findByDeviceNameAndLocation(String deviceName, String location);

    
// }

// DeviceRepository.java (updated)
package com.mediaserver.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mediaserver.dto.MonthlyAggregate;
import com.mediaserver.dto.WeeklyAggregate;
import com.mediaserver.model.Device;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {
    Optional<Device> findByMacAddress(String macAddress);
    List<Device> findByUserId(String userId);
    boolean existsByMacAddress(String macAddress);
    boolean existsById(String id);
    //List<Device> findByStatus(String status);
    List<Device> findByUserIdAndStatus(String userId, String status);
    //Optional<Device> findById(String id);
    // added 11/6
    Optional<Device> findById(String deviceId);
    List<Device> findAllById(List<String> ids);
    List<Device> findByGroupId(String groupId);

    // add the repo for filter part 18.6.25
    List<Device> findByDeviceName(String deviceName);
    List<Device> findByDeviceNameAndEnabled(String deviceName, Boolean enabled);
    List<Device> findByStatus(String status);
    List<Device> findByDeviceNameAndStatusAndEnabled(String deviceName, String status, boolean enabled);
    List<Device> findByStatusAndLocation(String status, String location);
    List<Device> findByDeviceNameAndLocation(String deviceName, String location);
 
    boolean existsByMacAddressAndUserId(String macAddress, String userId);
    Optional<Device> findByMacAddressAndUserId(String macAddress, String userId);

    /* same two aggregations but on registrationDate field */
    /* Weekly aggregation pipeline */
    @Aggregation(pipeline = {
        "{ $match: { userId: ?0, registrationDate : { $gte : ?1 } } }",
        "{ $group: { _id : { $dayOfWeek : '$registrationDate' }, deviceHits : { $sum : 1 } } }"
    })
    List<WeeklyAggregate> getWeeklyStats(String userId, LocalDateTime since);

    /* Monthly aggregation pipeline (optional, for completeness) */
    @Aggregation(pipeline = {
        "{ $match: { userId: ?0, registrationDate : { $gte : ?1 } } }",
        "{ $group: { _id : { $month : '$registrationDate' }, deviceHits : { $sum : 1 } } }"
    })
    List<MonthlyAggregate> getMonthlyStats(String userId, LocalDateTime since);

    @Aggregation(pipeline = {
    "{ $match: { userId: ?0, registrationDate : { $gte : ?1, $lte : ?2 } } }",
    "{ $group: { _id : { $dayOfWeek : '$registrationDate' }, deviceHits : { $sum : 1 } } }"
})
List<WeeklyAggregate> getWeeklyStats(String userId, LocalDateTime from, LocalDateTime to);

@Aggregation(pipeline = {
    "{ $match: { userId: ?0, registrationDate : { $gte : ?1, $lte : ?2 } } }",
    "{ $group: { _id : { year: { $year : '$registrationDate' }, month: { $month : '$registrationDate' } }, deviceHits : { $sum : 1 } } }",
    "{ $project: { year: '$_id.year', month: '$_id.month', deviceHits: 1, _id: 0 } }",
    "{ $sort: { year : 1, month : 1 } }"
})
List<MonthlyAggregate> getMonthlyStats(String userId, LocalDateTime from, LocalDateTime to);

@Aggregation(pipeline = {
    "{ $match: { registrationDate : { $gte : ?0, $lte : ?1 } } }",
    "{ $group: { _id : { $dayOfWeek : '$registrationDate' }, deviceHits : { $sum : 1 } } }"
})
List<WeeklyAggregate> getWeeklyStatsForAllUsers(LocalDateTime from, LocalDateTime to);

@Aggregation(pipeline = {
    "{ $match: { registrationDate : { $gte : ?0, $lte : ?1 } } }",
    "{ $group: { _id : { year: { $year : '$registrationDate' }, month: { $month : '$registrationDate' } }, deviceHits : { $sum : 1 } } }",
    "{ $project: { year: '$_id.year', month: '$_id.month', deviceHits: 1, _id: 0 } }",
    "{ $sort: { year : 1, month : 1 } }"
})
List<MonthlyAggregate> getMonthlyStatsForAllUsers(LocalDateTime from, LocalDateTime to);
}