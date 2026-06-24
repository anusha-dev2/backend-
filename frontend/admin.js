const BASE_URL = 'http://localhost:9000';

// Helper to show messages
function showMessage(elementId, message, isError = false) {
    const el = document.getElementById(elementId);
    el.textContent = message;
    el.className = `message ${isError ? 'error' : 'success'}`;
    el.classList.remove('hidden');

    // Auto-hide after 5 seconds
    setTimeout(() => {
        el.classList.add('hidden');
    }, 5000);
}

// 1. Group Devices
document.getElementById('groupForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const groupId = document.getElementById('groupId').value.trim();
    const devicesInput = document.getElementById('deviceIds').value;

    // Parse comma-separated devices
    const devices = devicesInput.split(',').map(d => d.trim()).filter(d => d);

    if (!groupId || devices.length === 0) {
        showMessage('groupMessage', 'Please provide a valid Group ID and at least one Device ID.', true);
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/groups/${groupId}/devices/add`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ Devices: devices })
        });

        if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText || `Server responded with status: ${response.status}`);
        }

        showMessage('groupMessage', `Successfully assigned ${devices.length} devices to ${groupId}.`);

        // Auto-fill trigger form
        document.getElementById('triggerGroupId').value = groupId;

    } catch (error) {
        showMessage('groupMessage', error.message, true);
    }
});

// 2. Trigger Video Wall
document.getElementById('triggerForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const groupId = document.getElementById('triggerGroupId').value.trim();
    const videoId = document.getElementById('videoId').value.trim();

    if (!groupId || !videoId) {
        showMessage('triggerMessage', 'Please provide a Group ID and Video ID.', true);
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/groups/${groupId}/video-wall/process`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ videoId: videoId })
        });

        if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText || `Server responded with status: ${response.status}`);
        }

        showMessage('triggerMessage', 'Video Wall processing initiated successfully!');

    } catch (error) {
        showMessage('triggerMessage', error.message, true);
    }
});
