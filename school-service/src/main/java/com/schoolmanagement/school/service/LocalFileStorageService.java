package com.schoolmanagement.school.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class LocalFileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public static final String MODULE_LOGOS = "logos";
    public static final String MODULE_STUDY_MATERIALS = "study-materials";
    public static final String MODULE_SYLLABUS = "syllabus";
    public static final String MODULE_LECTURES = "lectures";
    public static final String MODULE_PROFILES = "profiles";

    private Path rootPath;

    @PostConstruct
    public void init() {
        try {
            rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(rootPath);
            Files.createDirectories(rootPath.resolve(MODULE_LOGOS));
            Files.createDirectories(rootPath.resolve(MODULE_STUDY_MATERIALS));
            Files.createDirectories(rootPath.resolve(MODULE_SYLLABUS));
            Files.createDirectories(rootPath.resolve(MODULE_LECTURES));
            Files.createDirectories(rootPath.resolve(MODULE_PROFILES));
            System.out.println("Local File Storage initialized at: " + rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize local file storage directories!", e);
        }
    }

    public Path getModulePath(String module) {
        return rootPath.resolve(module).normalize();
    }

    /**
     * Stores a multipart file into the designated module folder.
     * Returns the relative web URL path: /uploads/{module}/{filename}
     */
    public String storeFile(MultipartFile file, String module, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Cannot store empty file.");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String safeName = (originalFilename != null)
                    ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "-")
                    : "file" + extension;

            String uniqueFilename = (prefix != null && !prefix.isBlank() ? prefix + "_" : "")
                    + System.currentTimeMillis() + "_" + safeName;

            Path targetLocation = getModulePath(module).resolve(uniqueFilename).normalize();

            // Security check
            if (!targetLocation.startsWith(rootPath)) {
                throw new RuntimeException("Cannot store file outside current storage directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return relative URL that will be served via WebMvcConfig
            return "/uploads/" + module + "/" + uniqueFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    /**
     * Stores an existing java.io.File into the designated module folder.
     */
    public String storeFile(File file, String module, String filename) {
        if (file == null || !file.exists()) {
            throw new RuntimeException("Source file does not exist.");
        }

        try {
            Path targetLocation = getModulePath(module).resolve(filename).normalize();
            Files.copy(file.toPath(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + module + "/" + filename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Deletes a file by its URL or relative path.
     */
    public boolean deleteFile(String fileUrlOrPath) {
        if (fileUrlOrPath == null || fileUrlOrPath.isBlank()) {
            return false;
        }

        try {
            String relative = fileUrlOrPath;
            if (relative.startsWith("/uploads/")) {
                relative = relative.substring("/uploads/".length());
            } else if (relative.startsWith("uploads/")) {
                relative = relative.substring("uploads/".length());
            }

            Path filePath = rootPath.resolve(relative).normalize();
            if (Files.exists(filePath) && filePath.startsWith(rootPath)) {
                return Files.deleteIfExists(filePath);
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Loads a file as a Resource for downloading or streaming.
     */
    public Resource loadFileAsResource(String fileUrlOrPath) {
        try {
            String relative = fileUrlOrPath;
            if (relative.startsWith("/uploads/")) {
                relative = relative.substring("/uploads/".length());
            } else if (relative.startsWith("uploads/")) {
                relative = relative.substring("uploads/".length());
            }

            Path filePath = rootPath.resolve(relative).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileUrlOrPath);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File path invalid: " + fileUrlOrPath, ex);
        }
    }

    public Path getRootPath() {
        return rootPath;
    }
}
