package com.example.WarmTea.Controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3-test")
public class S3Controller_TEST {

    private final AmazonS3 s3Client;

    // 1. Проверка подключения: вывод списка бакетов
    @GetMapping("/buckets")
    public ResponseEntity<List<String>> listBuckets() {
        List<String> buckets = s3Client.listBuckets()
                .stream()
                .map(Bucket::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(buckets);
    }

    // 2. Список файлов в бакете
    @GetMapping("/objects")
    public ResponseEntity<List<String>> listObjects(@RequestParam String bucket) {
        List<String> objects = s3Client.listObjects(bucket)
                .getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());

        return ResponseEntity.ok(objects);
    }

    // 3. Загрузка файла в бакет
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(
            @RequestParam String bucket,
            @RequestParam MultipartFile file) throws IOException {

        s3Client.putObject(new PutObjectRequest(
                bucket,
                file.getOriginalFilename(),
                file.getInputStream(),
                new ObjectMetadata()
        ));

        return ResponseEntity.ok("Файл загружен: " + file.getOriginalFilename());
    }

    // 4. Скачивание файла
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(
            @RequestParam String bucket,
            @RequestParam String key) throws IOException {

        S3Object object = s3Client.getObject(bucket, key);
        byte[] bytes = object.getObjectContent().readAllBytes();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + key)
                .body(bytes);
    }

    // 5. Удаление файла
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(
            @RequestParam String bucket,
            @RequestParam String key) {

        s3Client.deleteObject(bucket, key);
        return ResponseEntity.ok("Файл удалён: " + key);
    }
}
