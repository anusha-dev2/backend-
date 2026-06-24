package com.mediaserver.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class MonthlyAggregate {
    private Integer year;
    private Integer month;
    private Long contentPlays;
    private Double bandwidthGb;
    private Long deviceHits;

    public Integer getYear() { return year; }
    public Integer getMonth() { return month; }
    public Long getContentPlays() { return contentPlays; }
    public Double getBandwidthGb() { return bandwidthGb; }
    public Long getDeviceHits() { return deviceHits; }
}
