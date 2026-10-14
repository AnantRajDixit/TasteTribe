package com.tastetribe.service;

import com.tastetribe.exception.BadRequestException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores uploaded recipe cover photos on the server's filesystem and returns the
 * public URL that serves them back.
 *
 * <p>Security: the client-supplied filename is never trusted — only its extension is
 * read (and must be in an image whitelist), and the stored name is a fresh UUID. That
 * removes any path-traversal or executable-upload surface.</p>
 */
@Service
public class ImageStorageService {

    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "webp", "gif", "avif");
    private static final long MAX_BYTES = 8L * 1024 * 1024; // 8 MB

    private final Path root;

    public ImageStorageService(@Value("${app.uploads.dir:./uploads}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
    }

    /**
     * Persist the upload and return its servable path (e.g. {@code /api/uploads/abc.jpg}).
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose an image to upload.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("That image is larger than 8 MB. Please choose a smaller one.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("Only image files can be uploaded.");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED.contains(extension)) {
            throw new BadRequestException("Supported image types are JPG, PNG, WEBP, GIF and AVIF.");
        }

        String storedName = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(root);
            Path target = root.resolve(storedName).normalize();
            // Defence in depth: the resolved path must stay inside the upload root.
            if (!target.startsWith(root)) {
                throw new BadRequestException("That filename is not allowed.");
            }
            try (var input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new BadRequestException("We could not save that image. Please try again.");
        }
        return "/api/uploads/" + storedName;
    }

    /** Lowercased extension without the dot, or empty when there is none. */
    private static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public Path rootDir() {
        return root;
    }
}
