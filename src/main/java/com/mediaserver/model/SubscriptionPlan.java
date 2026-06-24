// package com.mediaserver.model;

// import java.util.List;

// import org.springframework.data.annotation.Id;
// import org.springframework.data.mongodb.core.index.Indexed;
// import org.springframework.data.mongodb.core.mapping.Document;
// import org.springframework.data.mongodb.core.mapping.Field;

// import com.fasterxml.jackson.annotation.JsonProperty;

// import lombok.Data;

// @Data
// @Document(collection = "subscription_plans")
// public class SubscriptionPlan {
//     @Id
//     private String id;

//     @Indexed(unique = true)
//     private String planCode; // Human-readable code like "basic_monthly"

//     private String name;

//     private String description;

//     private Double price;

//     private String interval;  // "MONTHLY" or "YEARLY"

//     @Indexed(unique = true)
//     @Field("PriceId")
//     @JsonProperty("priceId")
//     private String PriceId;

//     private List<String> features;

//     private Integer maxContent; // Maximum number of content items allowed
//     private Integer maxPlaylists; // Maximum number of playlists allowed
//     private Integer maxDevices; // Maximum number of devices allowed
//     private Integer maxGroups; // Maximum number of groups allowed

//     // Getters and setters
//     public String getId() {
//         return id;
//     }

//     public void setId(String id) {
//         this.id = id;
//     }

//     public String getPlanCode() {
//         return planCode;
//     }

//     public void setPlanCode(String planCode) {
//         this.planCode = planCode;
//     }

//     public String getName() {
//         return name;
//     }

//     public void setName(String name) {
//         this.name = name;
//     }

//     public String getDescription() {
//         return description;
//     }

//     public void setDescription(String description) {
//         this.description = description;
//     }

//     public Double getPrice() {
//         return price;
//     }

//     public void setPrice(Double price) {
//         this.price = price;
//     }

//     public String getInterval() {
//         return interval;
//     }

//     public void setInterval(String interval) {
//         this.interval = interval;
//     }

//     @JsonProperty("priceId")
//     public String getPriceId() {
//         return PriceId;
//     }

//     @JsonProperty("priceId")
//     public void setPriceId(String PriceId) {
//         this.PriceId = PriceId;
//     }

//     public List<String> getFeatures() {
//         return features;
//     }

//     public void setFeatures(List<String> features) {
//         this.features = features;
//     }

//     public Integer getMaxContent() {
//         return maxContent;
//     }

//     public void setMaxContent(Integer maxContent) {
//         this.maxContent = maxContent;
//     }

//     public Integer getMaxPlaylists() {
//         return maxPlaylists;
//     }

//     public void setMaxPlaylists(Integer maxPlaylists) {
//         this.maxPlaylists = maxPlaylists;
//     }

//     public Integer getMaxDevices() {
//         return maxDevices;
//     }

//     public void setMaxDevices(Integer maxDevices) {
//         this.maxDevices = maxDevices;
//     }

//     public Integer getMaxGroups() {
//         return maxGroups;
//     }

//     public void setMaxGroups(Integer maxGroups) {
//         this.maxGroups = maxGroups;
//     }
// }


package com.mediaserver.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Document(collection = "subscription_plans")
public class SubscriptionPlan {
    @Id
    private String id;

    @Indexed(unique = true)
    private String planCode; // Human-readable code like "basic_monthly"

    private String name;

    private String description;

    private Double price;

    private String interval;  // "MONTHLY" or "YEARLY"

    @Indexed(unique = true)
    @Field("priceId")  // Changed: Use lowercase in MongoDB
    @JsonProperty("priceId")  // Changed: Use lowercase in JSON
    private String priceId;  // Changed: Use lowercase field name

    private List<String> features;

    private Integer maxContent; // Maximum number of content items allowed
    private Integer maxPlaylists; // Maximum number of playlists allowed
    private Integer maxDevices; // Maximum number of devices allowed
    private Integer maxGroups; // Maximum number of groups allowed

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public Integer getMaxContent() {
        return maxContent;
    }

    public void setMaxContent(Integer maxContent) {
        this.maxContent = maxContent;
    }

    public Integer getMaxPlaylists() {
        return maxPlaylists;
    }

    public void setMaxPlaylists(Integer maxPlaylists) {
        this.maxPlaylists = maxPlaylists;
    }

    public Integer getMaxDevices() {
        return maxDevices;
    }

    public void setMaxDevices(Integer maxDevices) {
        this.maxDevices = maxDevices;
    }

    public Integer getMaxGroups() {
        return maxGroups;
    }

    public void setMaxGroups(Integer maxGroups) {
        this.maxGroups = maxGroups;
    }
}