package dev.m4nd3l.easysaves.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Handles AES-GCM encryption and decryption flows, utilizing localized hardware profiles as the root master key.
 */
public class EasySavesSecurity {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;
    private static String masterKey;

    private static String getMasterKey() {
        if (masterKey == null) masterKey = getSafeMasterKey();
        return masterKey;
    }

    private static String getSafeMasterKey() {
        try { return HardwareKeyDeriver.generateMasterKey(); }
        catch (Exception exception) { return "FallbackDefaultSecureMasterKeyString"; }
    }

    /**
     * Encrypts a plaintext string and encodes the result in Base64 format.
     *
     * @param plaintext The text input to obscure.
     * @return An encrypted Base64 string payload containing the IV and cipher tags.
     */
    public static String encrypt(String plaintext) { return Base64.getEncoder().encodeToString(encrypt(plaintext.getBytes(StandardCharsets.UTF_8))); }

    /**
     * Encrypts raw bytes using AES/GCM/NoPadding with a randomized IV prefix payload block.
     *
     * @param plaintext The byte block array to encrypt.
     * @return Concat array containing initialization vectors followed by cipher output data blocks.
     */
    public static byte[] encrypt(byte[] plaintext) {
        try {
            byte[] initializationVector = new byte[IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(initializationVector);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(getMasterKey()), new GCMParameterSpec(TAG_LENGTH_BITS, initializationVector));
            byte[] encryptedBytes = cipher.doFinal(plaintext);

            byte[] combinedOutputs = new byte[initializationVector.length + encryptedBytes.length];
            System.arraycopy(initializationVector, 0, combinedOutputs, 0, initializationVector.length);
            System.arraycopy(encryptedBytes, 0, combinedOutputs, initializationVector.length, encryptedBytes.length);
            return combinedOutputs;
        } catch (Exception exception) { throw new RuntimeException("Encryption failed", exception); }
    }

    /**
     * Decrypts a Base64 string payload back into standard readable text format.
     *
     * @param encryptedBase64 The base64 combined data string.
     * @return Decrypted standard plaintext content payload string.
     */
    public static String decrypt(String encryptedBase64) { return new String(decrypt(Base64.getDecoder().decode(encryptedBase64)), StandardCharsets.UTF_8); }

    /**
     * Processes combined encrypted data blocks, separating the IV prefix to parse out and decrypt the payload.
     *
     * @param combinedOutputs Combined IV array and cipher byte segmentations block.
     * @return Decrypted clean plain text byte data structure array.
     */
    public static byte[] decrypt(byte[] combinedOutputs) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(combinedOutputs);

            byte[] initializationVector = new byte[IV_LENGTH_BYTES];
            buffer.get(initializationVector);
            byte[] encryptedBytes = new byte[buffer.remaining()];
            buffer.get(encryptedBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(getMasterKey()), new GCMParameterSpec(TAG_LENGTH_BITS, initializationVector));
            return cipher.doFinal(encryptedBytes);
        } catch (Exception exception) { throw new RuntimeException("Decryption failed", exception); }
    }

    private static SecretKey deriveKey(String secretMasterKey) throws Exception {
        byte[] keyBytes = new byte[32];
        byte[] secretBytes = secretMasterKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, "AES");
    }
}