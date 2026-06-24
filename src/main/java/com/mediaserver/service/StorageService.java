package com.mediaserver.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

@Service
public class StorageService {

    @Value("${content.storage.location}")
    private String storageLocation;

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }
        
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String filename = UUID.randomUUID().toString() + extension;
Path destinationPath = Paths.get(storageLocation, filename).normalize().toAbsolutePath();

if (!destinationPath.getParent().equals(Paths.get(storageLocation).normalize().toAbsolutePath())) {
            throw new IOException("Cannot store file outside current directory.");
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            System.out.println("Storing to: " + destinationPath);
            Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        return filename;
    }

    public ThumbnailResult generateThumbnail(String videoFilename) {
        String duration = "0";
        try {
            Path storagePath = Paths.get(storageLocation);
            Path videoPath = storagePath.resolve(videoFilename);
            String thumbnailFilename = UUID.randomUUID() + ".png";
            Path thumbnailPath = storagePath.resolve(thumbnailFilename);

            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", videoPath.toString(), "-vf", "thumbnail,scale=640:360", "-frames:v", "1", thumbnailPath.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\\n");
                    System.out.println("FFmpeg: " + line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Extract duration
                String outputStr = output.toString();
                Pattern pattern = Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2}\\.\\d{2})");
                Matcher matcher = pattern.matcher(outputStr);
                if (matcher.find()) {
                    int hours = Integer.parseInt(matcher.group(1));
                    int minutes = Integer.parseInt(matcher.group(2));
                    double seconds = Double.parseDouble(matcher.group(3));
                    duration = String.valueOf(hours * 3600 + minutes * 60 + seconds);
                }
                return new ThumbnailResult(thumbnailFilename, duration);
            } else {
                System.out.println("WARNING: FFmpeg exit " + exitCode + " - no thumbnail. Install ffmpeg.exe in PATH.");
                Files.deleteIfExists(thumbnailPath);
            }
        } catch (Exception e) {
            System.out.println("WARNING: Thumbnail generation failed: " + e.getMessage());
        }
        return new ThumbnailResult(null, duration);
    }

    public String generateImageThumbnail(String imageFilename) throws IOException {
        Path storagePath = Paths.get(storageLocation);
        Path imagePath = storagePath.resolve(imageFilename);
        String thumbnailFilename = "thumb_" + UUID.randomUUID() + ".png";
        Path thumbnailPath = storagePath.resolve(thumbnailFilename);

        try {
            BufferedImage originalImage = ImageIO.read(imagePath.toFile());
            if (originalImage == null) {
                throw new IOException("Could not read image file: " + imageFilename);
            }

            // Calculate thumbnail dimensions (max 640x360)
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int thumbnailWidth = 640;
            int thumbnailHeight = 360;

            // Maintain aspect ratio
            double aspectRatio = (double) originalWidth / originalHeight;
            if (aspectRatio > (double) thumbnailWidth / thumbnailHeight) {
                thumbnailHeight = (int) (thumbnailWidth / aspectRatio);
            } else {
                thumbnailWidth = (int) (thumbnailHeight * aspectRatio);
            }

            // Create thumbnail
            BufferedImage thumbnailImage = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnailImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
            g2d.dispose();

            // Save thumbnail
            ImageIO.write(thumbnailImage, "png", thumbnailPath.toFile());
            
            return thumbnailFilename;
        } catch (Exception e) {
            throw new IOException("Failed to generate image thumbnail", e);
        }
    }

    public String generatePdfThumbnail(String pdfFilename) throws IOException {
        Path storagePath = Paths.get(storageLocation);
        Path pdfPath = storagePath.resolve(pdfFilename);
        String thumbnailFilename = "pdf_thumb_" + UUID.randomUUID() + ".png";
        Path thumbnailPath = storagePath.resolve(thumbnailFilename);

        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            
            // Render first page as thumbnail
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 150, ImageType.RGB);
            
            // Resize to thumbnail size
            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();
            int thumbnailWidth = 640;
            int thumbnailHeight = 360;

            // Maintain aspect ratio
            double aspectRatio = (double) originalWidth / originalHeight;
            if (aspectRatio > (double) thumbnailWidth / thumbnailHeight) {
                thumbnailHeight = (int) (thumbnailWidth / aspectRatio);
            } else {
                thumbnailWidth = (int) (thumbnailHeight * aspectRatio);
            }

            BufferedImage thumbnailImage = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnailImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(image, 0, 0, thumbnailWidth, thumbnailHeight, null);
            g2d.dispose();

            // Save thumbnail
            ImageIO.write(thumbnailImage, "png", thumbnailPath.toFile());
            
            return thumbnailFilename;
        } catch (Exception e) {
            throw new IOException("Failed to generate PDF thumbnail", e);
        }
    }

    public int getPdfPageCount(String pdfFilename) throws IOException {
        Path storagePath = Paths.get(storageLocation);
        Path pdfPath = storagePath.resolve(pdfFilename);

        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            return document.getNumberOfPages();
        } catch (Exception e) {
            throw new IOException("Failed to get PDF page count", e);
        }
    }

    public String generateWebThumbnail(String url) throws IOException {
        // This is a simplified implementation
        // In a production environment, you might want to use a headless browser like Selenium or Puppeteer
        String thumbnailFilename = "web_thumb_" + UUID.randomUUID() + ".png";
        Path storagePath = Paths.get(storageLocation);
        Path thumbnailPath = storagePath.resolve(thumbnailFilename);

        try {
            // Try to use a web screenshot service or headless browser
            // For now, we'll create a placeholder thumbnail
            BufferedImage placeholderImage = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = placeholderImage.createGraphics();
            g2d.setColor(java.awt.Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, 640, 360);
            g2d.setColor(java.awt.Color.BLACK);
            g2d.drawString("Web Link", 280, 180);
            g2d.drawString(url.length() > 50 ? url.substring(0, 50) + "..." : url, 50, 200);
            g2d.dispose();

            ImageIO.write(placeholderImage, "png", thumbnailPath.toFile());
            return thumbnailFilename;
        } catch (Exception e) {
            throw new IOException("Failed to generate web thumbnail", e);
        }
    }

    public void delete(String filename) throws IOException {
        Path filePath = Paths.get(storageLocation).resolve(filename);
        Files.deleteIfExists(filePath);
    }

    public Resource loadAsResource(String filename) throws IOException {
        Path filePath = Paths.get(storageLocation).resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        throw new IOException("File not found or not readable: " + filename);
    }

    public boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isPdfFile(String contentType) {
        return "application/pdf".equals(contentType);
    }

    public boolean isVideoFile(String contentType) {
        return contentType != null && contentType.startsWith("video/");
    }
}
