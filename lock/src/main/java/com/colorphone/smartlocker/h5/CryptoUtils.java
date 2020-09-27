package com.colorphone.smartlocker.h5;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.ihs.commons.utils.HSLog;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    private static final String TAG = "CryptoUtils";
    private static final String ALGORITHM_AES = "AES";
    private static final String ALGORITHM_RSA = "RSA";

    @NonNull
    public static String encrypt(String data, String secretKey) {
        HSLog.d(TAG, "encrypt");
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM_AES);
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byte[] bytesToDecrypt = cipher.doFinal(data.getBytes());
            return Base64.encodeToString(bytesToDecrypt, Base64.DEFAULT);
        } catch (Exception e) {
            HSLog.d(TAG, "encrypt exception: " + e.getMessage());
            return "";
        }
    }

    @NonNull
    public static String decrypt(String data, String secretKey) {
        HSLog.d(TAG, "decrypt");
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM_AES);
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
            byte[] encryptedContent = Base64.decode(data.getBytes(), Base64.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byte[] original = cipher.doFinal(encryptedContent);
            return new String(original, Charset.forName("UTF-8"));
        } catch (Exception e) {
            HSLog.d(TAG, "decrypt exception: " + e.getMessage());
            return "";
        }
    }

    /**
     * Return the bytes of RSA decryption.
     *
     * @param data           The data.
     * @param privateKey     The private key.
     * @param keySize        The size of key, e.g. 1024, 2048...
     * @param transformation The name of the transformation, e.g., <i>RSA/CBC/PKCS1Padding</i>.
     * @return the bytes of RSA decryption
     */
    @Nullable
    public static byte[] decryptRSA(final byte[] data, final byte[] privateKey,
                                    final int keySize, final String transformation) {
        return rsaTemplate(data, privateKey, keySize, transformation, false);
    }

    /**
     * Return the bytes of RSA encryption.
     *
     * @param data           The data.
     * @param publicKey      The public key.
     * @param keySize        The size of key, e.g. 1024, 2048...
     * @param transformation The name of the transformation, e.g., <i>RSA/CBC/PKCS1Padding</i>.
     * @return the bytes of RSA encryption
     */
    @Nullable
    public static byte[] encryptRSA(final byte[] data, final byte[] publicKey,
                                    final int keySize, final String transformation) {
        return rsaTemplate(data, publicKey, keySize, transformation, true);
    }

    /**
     * Return the bytes of RSA encryption or decryption.
     *
     * @param data           The data.
     * @param key            The key.
     * @param keySize        The size of key, e.g. 1024, 2048...
     * @param transformation The name of the transformation, e.g., <i>DES/CBC/PKCS1Padding</i>.
     * @param isEncrypt      True to encrypt, false otherwise.
     * @return the bytes of RSA encryption or decryption
     */
    @Nullable
    private static byte[] rsaTemplate(final byte[] data, final byte[] key,
                                      final int keySize, final String transformation,
                                      final boolean isEncrypt) {
        if (data == null || data.length == 0 || key == null || key.length == 0) {
            return null;
        }
        try {
            Key rsaKey;
            if (isEncrypt) {
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
                rsaKey = KeyFactory.getInstance(ALGORITHM_RSA).generatePublic(keySpec);
            } else {
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
                rsaKey = KeyFactory.getInstance(ALGORITHM_RSA).generatePrivate(keySpec);
            }
            if (rsaKey == null) {
                return null;
            }
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, rsaKey);
            int len = data.length;
            int maxLen = keySize / 8;
            if (isEncrypt) {
                String lowerTrans = transformation.toLowerCase();
                if (lowerTrans.endsWith("pkcs1padding")) {
                    maxLen -= 11;
                }
            }
            int count = len / maxLen;
            if (count > 0) {
                byte[] ret = new byte[0];
                byte[] buff = new byte[maxLen];
                int index = 0;
                for (int i = 0; i < count; i++) {
                    System.arraycopy(data, index, buff, 0, maxLen);
                    ret = joins(ret, cipher.doFinal(buff));
                    index += maxLen;
                }
                if (index != len) {
                    int restLen = len - index;
                    buff = new byte[restLen];
                    System.arraycopy(data, index, buff, 0, restLen);
                    ret = joins(ret, cipher.doFinal(buff));
                }
                return ret;
            } else {
                return cipher.doFinal(data);
            }
        } catch (NoSuchAlgorithmException e) {
            HSLog.d(TAG, "rsaTemplate: NoSuchAlgorithmException=" + e.getMessage());
        } catch (NoSuchPaddingException e) {
            HSLog.d(TAG, "rsaTemplate: NoSuchPaddingException=" + e.getMessage());
        } catch (InvalidKeyException e) {
            HSLog.d(TAG, "rsaTemplate: InvalidKeyException=" + e.getMessage());
        } catch (BadPaddingException e) {
            HSLog.d(TAG, "rsaTemplate: BadPaddingException=" + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            HSLog.d(TAG, "rsaTemplate: IllegalBlockSizeException=" + e.getMessage());
        } catch (InvalidKeySpecException e) {
            HSLog.d(TAG, "rsaTemplate: InvalidKeySpecException=" + e.getMessage());
        }
        return null;
    }

    private static byte[] joins(final byte[] prefix, final byte[] suffix) {
        byte[] ret = new byte[prefix.length + suffix.length];
        System.arraycopy(prefix, 0, ret, 0, prefix.length);
        System.arraycopy(suffix, 0, ret, prefix.length, suffix.length);
        return ret;
    }
}