package com.mediaserver.service;

import com.mediaserver.model.Video;
import com.mediaserver.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class VideoService {
    @Autowired
    private VideoRepository videoRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    public Video uploadVideo(MultipartFile file, String title, String description) throws IOException {
        String fileName = file.getOriginalFilename();
        String filePath = uploadDir + fileName;
        Path path = Paths.get(filePath);

        // Create the directory if it doesn't exist
        Files.createDirectories(path.getParent());

        File destFile = new File(filePath);
        file.transferTo(destFile);

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoUrl(baseUrl + fileName);

        return videoRepository.save(video);
    }

    public Optional<Video> getVideoById(String id) {
        return videoRepository.findById(id);
    }
}