const BASE_URL = 'http://localhost:9000';
let stompClient = null;
let currentDeviceId = null;

const connectBtn = document.getElementById('connectBtn');
const deviceIdInput = document.getElementById('deviceIdInput');
const statusEl = document.getElementById('connectionStatus');
const setupOverlay = document.getElementById('setupOverlay');
const videoContainer = document.getElementById('videoContainer');
const wallVideo = document.getElementById('wallVideo');
const debugInfo = document.getElementById('debugInfo');

connectBtn.addEventListener('click', () => {
    const deviceId = deviceIdInput.value.trim();
    if (!deviceId) {
        alert("Please enter a device ID.");
        return;
    }

    currentDeviceId = deviceId;
    connectWebSocket(deviceId);
});

function connectWebSocket(deviceId) {
    statusEl.textContent = 'Connecting...';
    statusEl.style.color = '#f0b37e'; // Yellow-ish

    // Connect to Spring Boot WebSocket Endpoint
    const socket = new SockJS(`${BASE_URL}/ws`);
    stompClient = Stomp.over(socket);

    // Disable debug logging in production
    stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        statusEl.textContent = 'Connected. Waiting for playback signal...';
        statusEl.style.color = '#3fb950'; // Green

        // Hide setup overlay after 1 second
        setTimeout(() => {
            setupOverlay.classList.add('hidden');
            videoContainer.classList.remove('hidden');
        }, 1000);

        // Subscribe to device-specific topic
        stompClient.subscribe(`/topic/device/${deviceId}`, function (message) {
            try {
                const payload = JSON.parse(message.body);
                handlePayload(payload);
            } catch (e) {
                console.error("Error parsing message payload:", e);
            }
        });

    }, function (error) {
        console.error("STOMP error:", error);
        statusEl.textContent = 'Connection failed. Retrying...';
        statusEl.style.color = '#ff7b72'; // Red

        // Retry connection after 5 seconds
        setTimeout(() => connectWebSocket(deviceId), 5000);
    });
}

function handlePayload(payload) {
    if (payload.action === 'PLAY_SYNC') {
        const syncStartTime = payload.startTime;
        const slicedVideoName = payload.contentId;

        // Video path (Assuming standard static folder delivery from backend)
        // Adjust the URL if the content is served from a different domain or path
        const localFilePath = `${BASE_URL}/${slicedVideoName}`;

        // 1. Calculate how long to wait
        const currentTime = Date.now();
        const timeToWait = syncStartTime - currentTime;

        // Display debug info
        debugInfo.classList.remove('hidden');
        debugInfo.innerHTML = `
            <div>Device: ${currentDeviceId}</div>
            <div>Video: ${slicedVideoName}</div>
            <div>Sync Time: ${new Date(syncStartTime).toISOString()}</div>
            <div>Delay: ${timeToWait}ms</div>
        `;

        // 2. Load the pre-sliced video into the player
        wallVideo.src = localFilePath;
        wallVideo.load();

        // 3. Set a precise timeout to trigger playback
        if (timeToWait > 0) {
            setTimeout(() => {
                wallVideo.play().catch(e => console.error("Playback failed:", e));
            }, timeToWait);
        } else {
            // If the time has already passed due to network lag, play immediately
            wallVideo.play().catch(e => console.error("Playback failed:", e));
        }
    }
}

// Fullscreen toggle on double click
videoContainer.addEventListener('dblclick', () => {
    if (!document.fullscreenElement) {
        videoContainer.requestFullscreen().catch(err => {
            console.error(`Error attempting to enable fullscreen: ${err.message}`);
        });
    } else {
        document.exitFullscreen();
    }
});
