package com.tastetribe.controller;

import com.tastetribe.service.ImageStorageService;
import com.tastetribe.web.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Cover-photo uploads — {@code POST /api/uploads/image}.
 *
 * <p>Login is required, so anonymous visitors cannot fill the disk. The response
 * carries the public URL which the client then stores as the recipe's coverImage.</p>
 */
@RestController
@RequestMapping("/uploads")
public class UploadController {

    private final ImageStorageService storage;
    private final SessionContext session;

    public UploadController(ImageStorageService storage, SessionContext session) {
        this.storage = storage;
        this.session = session;
    }

    @PostMapping("/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) {
        session.require(request);
        String url = storage.store(file);
        return Map.of("url", url);
    }
}
