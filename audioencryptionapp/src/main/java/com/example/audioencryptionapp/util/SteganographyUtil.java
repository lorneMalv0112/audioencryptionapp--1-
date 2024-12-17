// src/main/java/com/example/audioencryptionapp/util/SteganographyUtil.java
package com.example.audioencryptionapp.util;

import javax.sound.sampled.*;
import java.io.*;

public class SteganographyUtil {

    /**
     * Встраивает зашифрованное сообщение в аудио файл с помощью LSB стеганографии.
     *
     * @param inputAudioPath  путь к исходному аудио файлу
     * @param outputAudioPath путь к результирующему аудио файлу с встроенным сообщением
     * @param message         зашифрованное сообщение для встраивания
     * @throws Exception если произошла ошибка при обработке
     */
    public static void embedMessage(String inputAudioPath, String outputAudioPath, String message) throws Exception {
        File inputFile = new File(inputAudioPath);
        File outputFile = new File(outputAudioPath);

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputFile);
        AudioFormat format = audioInputStream.getFormat();

        byte[] audioBytes = audioInputStream.readAllBytes();
        audioInputStream.close();

        // Преобразуем сообщение в байты и добавим специальный конец сообщения
        byte[] messageBytes = (message + "###").getBytes(); // "###" — конец сообщения

        // Проверяем, достаточно ли места для сообщения
        if (messageBytes.length * 8 + 32 > audioBytes.length) { // 32 бита для хранения длины сообщения
            throw new Exception("Сообщение слишком длинное для данного аудио файла.");
        }

        // Записываем длину сообщения в первые 32 бита
        int messageLength = messageBytes.length;
        for (int i = 0; i < 32; i++) {
            int bit = (messageLength >> i) & 1;
            audioBytes[i] = (byte) ((audioBytes[i] & 0xFE) | bit);
        }

        // Записываем само сообщение
        for (int i = 0; i < messageBytes.length; i++) {
            byte b = messageBytes[i];
            for (int bit = 0; bit < 8; bit++) {
                int bitValue = (b >> bit) & 1;
                audioBytes[32 + i * 8 + bit] = (byte) ((audioBytes[32 + i * 8 + bit] & 0xFE) | bitValue);
            }
        }

        // Записываем изменённые байты в новый аудио файл
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        AudioInputStream modifiedAudioInputStream = new AudioInputStream(bais, format, audioBytes.length / format.getFrameSize());

        AudioSystem.write(modifiedAudioInputStream, AudioFileFormat.Type.WAVE, outputFile);
        modifiedAudioInputStream.close();
    }

    /**
     * Извлекает зашифрованное сообщение из аудио файла, используя LSB стеганографию.
     *
     * @param inputAudioPath путь к аудио файлу с встроенным сообщением
     * @return извлечённое зашифрованное сообщение
     * @throws Exception если произошла ошибка при обработке
     */
    public static String extractMessage(String inputAudioPath) throws Exception {
        File inputFile = new File(inputAudioPath);

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputFile);
        AudioFormat format = audioInputStream.getFormat();

        byte[] audioBytes = audioInputStream.readAllBytes();
        audioInputStream.close();

        // Читаем длину сообщения из первых 32 бит
        int messageLength = 0;
        for (int i = 0; i < 32; i++) {
            int bit = audioBytes[i] & 1;
            messageLength |= (bit << i);
        }

        if (messageLength <= 0 || messageLength > (audioBytes.length - 32) / 8) {
            throw new Exception("Некорректная длина сообщения или сообщение отсутствует.");
        }

        byte[] messageBytes = new byte[messageLength];
        for (int i = 0; i < messageLength; i++) {
            byte b = 0;
            for (int bit = 0; bit < 8; bit++) {
                int bitValue = audioBytes[32 + i * 8 + bit] & 1;
                b |= (bitValue << bit);
            }
            messageBytes[i] = b;
        }

        String messageWithEnd = new String(messageBytes);
        // Удаляем специальный конец сообщения
        if (messageWithEnd.contains("###")) {
            return messageWithEnd.substring(0, messageWithEnd.indexOf("###"));
        } else {
            return messageWithEnd; // Конец сообщения не найден
        }
    }
}

