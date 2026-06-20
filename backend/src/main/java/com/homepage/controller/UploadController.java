package com.homepage.controller;

import com.homepage.dto.Result;
import com.homepage.exception.BusinessException;
import com.homepage.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.security.admin-ips}")
    private String adminIps;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Set<String> allowed = Set.of(adminIps.split(","));
        if (!allowed.contains(ip) && !allowed.contains("::ffff:" + ip)) {
            return Result.error(ErrorCode.BAD_REQUEST, "forbidden: ip not allowed");
        }

        String ext = getName(file);

        String filename = UUID.randomUUID().toString() + ext;
        String userHome = System.getProperty("user.home");
        String dirPath = userHome + File.separator + uploadPath.replace("/", File.separator);
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(dir, filename));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "upload failed");
        }

        return Result.success(filename);
    }

    @NonNull
    private static String getName(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "file is empty");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid file");
        }

        String name = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        Set<String> allowedExt = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
        if (!allowedExt.contains(name)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "only image files allowed: jpg, jpeg, png, webp, gif");
        }
        return name;
    }
}
