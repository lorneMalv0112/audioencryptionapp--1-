// src/main/java/com/example/audioencryptionapp/service/SteganographyService.java
package com.example.audioencryptionapp.service;

import com.example.audioencryptionapp.util.EncryptionUtil;
import com.example.audioencryptionapp.util.SteganographyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class SteganographyService {

    private final Path storageLocation;
    private final EncryptionUtil encryptionUtil;

    public SteganographyService(@Value("${file.storage.location}") String storageLocation,
                                @Value("${encryption.key}") String encryptionKey) throws Exception {
        this.storageLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        if (!Files.exists(this.storageLocation)) {
            Files.createDirectories(this.storageLocation);
            log.info("Создана директория для хранения файлов: {}", this.storageLocation.toString());
        }
        this.encryptionUtil = new EncryptionUtil(encryptionKey);
    }

    /**
     * Встраивает зашифрованное сообщение в аудио файл.
     *
     * @param fileId  идентификатор файла
     * @param message текстовое сообщение для встраивания
     * @return имя файла с встроенным сообщением
     * @throws Exception при ошибках
     */
    public String embedMessage(String fileId, String message) throws Exception {
        if (message.length() > 1000) { // Ограничение на 1000 символов
            log.error("Сообщение слишком длинное. Длина: {}", message.length());
            throw new Exception("Сообщение слишком длинное. Максимальная длина: 1000 символов.");
        }

        Path originalFilePath = findOriginalFile(fileId);
        if (originalFilePath == null) {
            log.error("Оригинальный файл не найден. ID: {}", fileId);
            throw new Exception("Оригинальный файл не найден.");
        }

        // Шифруем сообщение
        String encryptedMessage = encryptionUtil.encrypt(message);
        log.info("Сообщение успешно зашифровано.");

        // Формируем имя файла с сообщением
        String originalFilename = originalFilePath.getFileName().toString();
        String outputFilename = fileId + "_with_message_" + originalFilename;
        Path outputFilePath = storageLocation.resolve(outputFilename);

        // Встраиваем сообщение
        SteganographyUtil.embedMessage(originalFilePath.toString(), outputFilePath.toString(), encryptedMessage);
        log.info("Сообщение успешно встроено в файл: {}", outputFilePath.toString());

        return outputFilename;
    }

    /**
     * Извлекает и дешифрует сообщение из аудио файла.
     *
     * @param fileId идентификатор файла
     * @return извлечённое сообщение
     * @throws Exception при ошибках
     */
    public String extractMessage(String fileId) throws Exception {
        Path fileWithMessagePath = findFileWithMessage(fileId);
        if (fileWithMessagePath == null) {
            log.error("Файл с сообщением не найден. ID: {}", fileId);
            throw new Exception("Файл с сообщением не найден.");
        }

        // Извлекаем зашифрованное сообщение
        String encryptedMessage = SteganographyUtil.extractMessage(fileWithMessagePath.toString());
        log.info("Сообщение успешно извлечено из файла.");

        // Дешифруем сообщение
        String decryptedMessage = encryptionUtil.decrypt(encryptedMessage);
        log.info("Сообщение успешно дешифровано.");

        return decryptedMessage;
    }

    /**
     * Сохраняет загруженный аудио файл и возвращает его идентификатор.
     *
     * @param file аудио файл
     * @return идентификатор файла
     * @throws Exception при ошибках
     */
    public String saveFile(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.error("Имя файла отсутствует.");
            throw new Exception("Имя файла отсутствует.");
        }

        // Проверяем, является ли файл WAV
        if (!originalFilename.toLowerCase().endsWith(".wav")) {
            log.error("Неподдерживаемый формат файла: {}", originalFilename);
            throw new Exception("Только WAV файлы поддерживаются для стеганографии.");
        }

        String fileId = UUID.randomUUID().toString();
        String filename = fileId + "_" + originalFilename;
        Path targetLocation = storageLocation.resolve(filename);

        // Сохраняем файл
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        log.info("Файл успешно сохранён: {}", targetLocation.toString());

        return fileId;
    }

    /**
     * Находит оригинальный файл по ID.
     *
     * @param fileId идентификатор файла
     * @return путь к файлу или null, если не найден
     * @throws Exception при ошибках
     */
    private Path findOriginalFile(String fileId) throws Exception {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageLocation, fileId + "_*.wav")) {
            for (Path path : stream) {
                String filename = path.getFileName().toString();
                if (!filename.contains("_with_message_")) {
                    return path;
                }
            }
        }
        return null;
    }

    /**
     * Находит файл с сообщением по ID.
     *
     * @param fileId идентификатор файла
     * @return путь к файлу или null, если не найден
     * @throws Exception при ошибках
     */
    private Path findFileWithMessage(String fileId) throws Exception {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageLocation, fileId + "_with_message_*.wav")) {
            for (Path path : stream) {
                return path;
            }
        }
        return null;
    }
}
