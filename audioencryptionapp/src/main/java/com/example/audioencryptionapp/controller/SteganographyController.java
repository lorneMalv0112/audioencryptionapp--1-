// src/main/java/com/example/audioencryptionapp/controller/SteganographyController.java
package com.example.audioencryptionapp.controller;

import com.example.audioencryptionapp.service.SteganographyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Slf4j
public class SteganographyController {

    @Autowired
    private SteganographyService steganographyService;

    /**
     * Загружает аудио файл и возвращает его ID.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileId = steganographyService.saveFile(file);
            return ResponseEntity.ok().body("Файл успешно загружен. ID файла: " + fileId);
        } catch (Exception e) {
            log.error("Ошибка при загрузке файла: {}", e.getMessage());
            return ResponseEntity.status(500).body("Не удалось загрузить файл: " + e.getMessage());
        }
    }

    /**
     * Встраивает текстовое сообщение в аудио файл.
     */
    @PostMapping("/embed")
    public ResponseEntity<?> embedMessage(@RequestParam("fileId") String fileId,
                                          @RequestParam("message") String message) {
        try {
            String outputFilename = steganographyService.embedMessage(fileId, message);
            return ResponseEntity.ok().body("Сообщение успешно встроено. Имя файла: " + outputFilename);
        } catch (Exception e) {
            log.error("Ошибка при встраивании сообщения: {}", e.getMessage());
            return ResponseEntity.status(500).body("Не удалось встроить сообщение: " + e.getMessage());
        }
    }

    /**
     * Извлекает и дешифрует сообщение из аудио файла.
     */
    @PostMapping("/extract")
    public ResponseEntity<?> extractMessage(@RequestParam("fileId") String fileId) {
        try {
            String message = steganographyService.extractMessage(fileId);
            return ResponseEntity.ok().body("Извлечённое сообщение: " + message);
        } catch (Exception e) {
            log.error("Ошибка при извлечении сообщения: {}", e.getMessage());
            return ResponseEntity.status(500).body("Не удалось извлечь сообщение: " + e.getMessage());
        }
    }
}
