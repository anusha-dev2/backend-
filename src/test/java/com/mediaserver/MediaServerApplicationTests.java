package com.mediaserver;

import com.mediaserver.model.*;
import com.mediaserver.repository.*;
import com.mediaserver.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
class MediaServerApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DeviceRepository deviceRepository;

    @MockBean
    private ContentRepository contentRepository;

    @MockBean
    private PlaylistRepository playlistRepository;

    @MockBean
    private PlaylistContentRepository playlistContentRepository;

    @MockBean
    private StorageService storageService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private PlaylistService playlistService;

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
    }

    @Test
    void testUserService() {
        // Setup test data
        User user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        // Mock repository responses
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        // Test getUserById
        Optional<User> foundUser = userService.getUserById("1");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());

        // Test createUser
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");

        User createdUser = userService.createUser(newUser);
        assertNotNull(createdUser);
    }

    @Test
    void testDeviceService() {
        // Setup test data
        User user = new User();
        user.setId("1");

        Device device = new Device();
        device.setId("1");
        device.setMacAddress("00:1A:2B:3C:4D:5E");
        device.setDeviceName("Test Device");
        device.setUserId("1");
        device.setEnabled(true);

        List<Device> devices = Arrays.asList(device);

        // Mock repository responses
        when(deviceRepository.findById("1")).thenReturn(Optional.of(device));
        when(deviceRepository.findByUserId("1")).thenReturn(devices);
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        // Test getDeviceById
        Optional<Device> foundDevice = deviceService.getDeviceById("1");
        assertTrue(foundDevice.isPresent());
        assertEquals("00:1A:2B:3C:4D:5E", foundDevice.get().getMacAddress());

        // Test getDevicesByUserId
        List<Device> userDevices = deviceService.getDevicesByUserId("1");
        assertFalse(userDevices.isEmpty());
        assertEquals(1, userDevices.size());

        // Test createDevice
        Device newDevice = new Device();
        newDevice.setMacAddress("00:1A:2B:3C:4D:5F");
        newDevice.setDeviceName("New Device");
        newDevice.setUserId("1");

        Device createdDevice = deviceService.createDevice(newDevice);
        assertNotNull(createdDevice);
    }

    @Test
    void testContentService() throws IOException {
        // Setup test data
        User user = new User();
        user.setId("1");

        Content content = new Content();
        content.setId("1");
        content.setTitle("Test Video");
        content.setFilePath("test.mp4");
        content.setFileType("video/mp4");
        content.setFileSize(1024L);
        content.setUserId("1");
        content.setUploadDate(LocalDateTime.now());
        content.setTags(Arrays.asList("test", "video")); // Add tags for consistency

        List<Content> contents = Arrays.asList(content);

        // Create a mock MultipartFile
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "test.mp4",
            "video/mp4",
            "test content".getBytes()
        );

        // Mock repository responses
        when(contentRepository.findById("1")).thenReturn(Optional.of(content));
        when(contentRepository.findByUserIdOrderByUploadDateDesc("1")).thenReturn(contents);
        when(contentRepository.save(any(Content.class))).thenReturn(content);
        when(storageService.store(any(MultipartFile.class))).thenReturn("test.mp4");
        when(storageService.generateThumbnail(anyString())).thenReturn(new ThumbnailResult("thumbnail.jpg", "00:00:10"));
        doNothing().when(storageService).delete(anyString());

        // Test getContentById
        Optional<Content> foundContent = contentService.getContentById("1");
        assertTrue(foundContent.isPresent());
        assertEquals("Test Video", foundContent.get().getTitle());

        // Test getContentByUserId
        List<Content> userContents = contentService.getContentByUserId("1");
        assertFalse(userContents.isEmpty());
        assertEquals(1, userContents.size());

        // Test uploadContent
        Content uploadedContent = contentService.uploadContent(mockFile, "New Video", "1", "test,video");
        assertNotNull(uploadedContent);
    }

    @Test
    void testPlaylistService() {
        // Setup test data
        Playlist playlist = new Playlist();
        playlist.setId("1");
        playlist.setName("Test Playlist");
        playlist.setUserId("1");
        playlist.setCreatedDate(LocalDateTime.now());

        PlaylistContent playlistContent = new PlaylistContent();
        playlistContent.setId("1");
        playlistContent.setPlaylistId("1");
        playlistContent.setContentId("1");
        playlistContent.setDisplayOrder(1);

        List<Playlist> playlists = Arrays.asList(playlist);
        List<PlaylistContent> playlistContents = Arrays.asList(playlistContent);

        // Mock repository responses
        when(playlistRepository.findById("1")).thenReturn(Optional.of(playlist));
        when(playlistRepository.findByUserId("1")).thenReturn(playlists);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);
        when(playlistContentRepository.findByPlaylistIdOrderByDisplayOrder("1")).thenReturn(playlistContents);
        when(playlistContentRepository.save(any(PlaylistContent.class))).thenReturn(playlistContent);

        // Test getPlaylistById
        Optional<Playlist> foundPlaylist = playlistService.getPlaylistById("1");
        assertTrue(foundPlaylist.isPresent());
        assertEquals("Test Playlist", foundPlaylist.get().getName());

        // Test getPlaylistsByUserId
        List<Playlist> userPlaylists = playlistService.getPlaylistsByUserId("1");
        assertFalse(userPlaylists.isEmpty());
        assertEquals(1, userPlaylists.size());

        // Test createPlaylist
        Playlist newPlaylist = new Playlist();
        newPlaylist.setName("New Playlist");
        newPlaylist.setUserId("1");

        Playlist createdPlaylist = playlistService.createPlaylist(newPlaylist);
        assertNotNull(createdPlaylist);

        // Test getPlaylistContents
        List<PlaylistContent> contents = playlistService.getPlaylistContents("1");
        assertFalse(contents.isEmpty());
        assertEquals(1, contents.size());

        // Test addContentToPlaylist
        PlaylistContent newContent = playlistService.addContentToPlaylist("1", "2", 2);
        assertNotNull(newContent);
    }

    @Test
    void testAuthenticationFlow() {
        // Placeholder for authentication flow tests
    }

    @Test
    void testStripeIntegration() {
        // Placeholder for Stripe integration tests
    }
}
