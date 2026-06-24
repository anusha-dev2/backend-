# Frontend Implementation Guide: Dynamic Video Wall

This document provides everything the frontend and device player development team needs to know to integrate with the new Video Wall architecture.

**Important Note:** This frontend guide depends entirely on the new server-side logic being deployed. The server handles all the complex FFmpeg splitting and grid calculations.

## 1. Admin Dashboard (React / Vue)

The Admin Dashboard is responsible for grouping devices and triggering the Video Wall processing.

### A. Grouping Devices
You will use the existing device grouping API to group physical screens together. 

**Endpoint:** `PUT /groups/{groupId}/devices/add` 
**Payload:**
```json
{
  "Devices": ["device_id_1", "device_id_2", "device_id_3"]
}
```
*Note: The order of device IDs in the array determines their position in the video wall grid (read left-to-right, top-to-bottom).*

### B. Triggering Video Wall Processing
When the user assigns a video to this group and clicks "Create Video Wall", the frontend must make the following API call.

**Endpoint:** `POST /groups/{groupId}/video-wall/process` 
**Payload:**
```json
{
  "videoId": "video_12345"
}
```
**What happens on the server?** The server will automatically calculate the best grid size (e.g., 2x2 for 4 devices, 4x3 for 11 devices), run FFmpeg to split the video into exact portions, and assign the cropped videos specifically to each device.

## 2. Digital Signage Player App (Android / Web / Tizen)

The physical playback devices no longer need to calculate CSS crops or video offsets. The server feeds them pre-sliced, ready-to-play videos.

### A. Downloading Content
When the server assigns the sliced video, the device will receive a standard content update notification. The device should download this video file as usual and store it locally.

### B. WebSocket Synchronization (Crucial)
To ensure all screens play their segment at the exact same millisecond, the devices must listen to a WebSocket event.

**WebSocket Event:** `PLAY_SYNC` 
**Payload Example:**
```json
{
  "action": "PLAY_SYNC",
  "contentId": "slice_video_12345",
  "startTime": 1718600000000 
}
```

### C. Player Logic
When the `PLAY_SYNC` event is received, the player must not play the video immediately. Instead, it must calculate the delay:

```javascript
// 1. Receive the payload via WebSocket
const syncStartTime = payload.startTime; 

// 2. Calculate how long to wait
const currentTime = Date.now();
const timeToWait = syncStartTime - currentTime;

// 3. Load the pre-sliced video into the player
videoElement.src = localFilePath;
videoElement.load();

// 4. Set a precise timeout to trigger playback
if (timeToWait > 0) {
    setTimeout(() => {
        videoElement.play();
    }, timeToWait);
} else {
    // If the time has already passed due to network lag, play immediately
    videoElement.play();
}
```

## Summary for Frontend
- **Admin:** Call the `/video-wall/process` endpoint.
- **Player:** Wait for the `PLAY_SYNC` WebSocket command and use `setTimeout` on the provided `startTime` to ensure millisecond-perfect multi-screen playback.
