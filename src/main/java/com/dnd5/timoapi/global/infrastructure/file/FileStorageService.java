package com.dnd5.timoapi.global.infrastructure.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

@Service
public class FileStorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;

    public FileStorageService(S3Client s3Client, S3Properties properties) {
        this.s3Client = s3Client;
        this.bucket = properties.bucket();
        this.region = properties.region();
    }

    public String store(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(filename)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new UncheckedIOException("파일 업로드 실패", e);
        }

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + filename;
    }
}
