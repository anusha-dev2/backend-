package com.mediaserver.dto;

import lombok.Data;
import java.util.Map;

@Data
public class WeeklyAggregate {
    private Integer id; // Day of week (1 = Monday, 7 = Sunday)
    private Long contentPlays; // For ContentRepository
    private Double bandwidthGb; // Optional, for ContentRepository
    private Long deviceHits; // For DeviceRepository

    public Integer getId() { return id; }
    public Long getContentPlays() { return contentPlays; }
    public Double getBandwidthGb() { return bandwidthGb; }
    public Long getDeviceHits() { return deviceHits; }
}
