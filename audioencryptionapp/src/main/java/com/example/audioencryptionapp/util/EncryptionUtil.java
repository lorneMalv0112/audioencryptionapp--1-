// src/main/java/com/example/audioencryptionapp/util/EncryptionUtil.java
package com.example.audioencryptionapp.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";

    private final SecretKeySpec secretKeySpec;

    public EncryptionUtil(String key) {
        this.secretKeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
    }

    /**
     * Шифрует текстовое сообщение.
     *
     * @param strToEncrypt текст для шифрования
     * @return зашифрованный текст в формате Base64
     * @throws Exception при ошибках шифрования
     */
    public String encrypt(String strToEncrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Дешифрует зашифрованное сообщение.
     *
     * @param strToDecrypt зашифрованный текст в формате Base64
     * @return расшифрованный текст
     * @throws Exception при ошибках дешифрования
     */
    public String decrypt(String strToDecrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decodedBytes = Base64.getDecoder().decode(strToDecrypt);
        byte[] decrypted = cipher.doFinal(decodedBytes);
        return new String(decrypted, "UTF-8");
    }
}
