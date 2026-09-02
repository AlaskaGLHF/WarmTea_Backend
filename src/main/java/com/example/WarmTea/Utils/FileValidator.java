package com.example.WarmTea.Utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileValidator {

    private FileValidator() {}

    public static void validateFileExtension(MultipartFile file, List<String> allowedExtensions) {
        /*

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Файл не выбран");
        }

         */

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new RuntimeException("Файл не имеет расширения");
        }

        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        if (!allowedExtensions.contains(ext)) {
            throw new RuntimeException("Недопустимый формат файла: " + ext);
        }
    }
}

