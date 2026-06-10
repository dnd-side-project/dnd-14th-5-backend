package com.dnd5.timoapi.global.infrastructure.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private final String baseUrl;

    public FileStorageService(FileStorageProperties properties) {
        this.uploadDir = Paths.get(properties.dir()).toAbsolutePath().normalize();
        this.baseUrl = properties.baseUrl();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("업로드 디렉토리 생성 실패: " + this.uploadDir, e);
        }
    }

    public String store(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            Path target = uploadDir.resolve(filename);
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장 실패", e);
        }

        return baseUrl + "/uploads/" + filename;
    }
}
