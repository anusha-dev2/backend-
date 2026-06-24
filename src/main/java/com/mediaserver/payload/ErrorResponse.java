// src/main/java/com/mediaserver/payload/ErrorResponse.java
// (Adjust package if you prefer com.mediaserver.exception)

package com.mediaserver.payload;  // Or: package com.mediaserver.exception;

import com.fasterxml.jackson.annotation.JsonFormat;  // Optional: For pretty date formatting
import java.time.LocalDateTime;

public class ErrorResponse {
    private String message;
    private String timestamp;
    private int status;

    // Constructor
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();  // ISO format: 2025-09-12T11:07:10
        this.status = 400;  // Default to BAD_REQUEST; override if needed
    }

    // Constructor with status (for flexibility)
    public ErrorResponse(String message, int status) {
        this(message);
        this.status = status;
    }

    // Getters and Setters (add Lombok @Data if you use it for brevity)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}