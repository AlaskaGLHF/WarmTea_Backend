package com.example.WarmTea.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class S3Service {

    private final AmazonS3 s3Client;

    @Value("${yandex.s3.bucket-name}")
    private String bucketName;

    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    // === Streaming Upload ===
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) return null;

        try (InputStream inputStream = file.getInputStream()) {

            String key = folder + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());        // <= важно!!!
            metadata.setContentType(file.getContentType());

            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    key,
                    inputStream,
                    metadata
            );

            s3Client.putObject(request);

            return s3Client.getUrl(bucketName, key).toString();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла в S3: " + e.getMessage(), e);
        }
    }

    public String uploadMovieFile(MultipartFile file, Integer typeNumber) {
        if (file == null || file.isEmpty()) return null;

        String folder = (typeNumber != null && typeNumber == 2)
                ? "serials"
                : "films";

        return uploadFile(file, folder);
    }
}
