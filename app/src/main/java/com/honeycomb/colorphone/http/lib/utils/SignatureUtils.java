package com.honeycomb.colorphone.http.lib.utils;

public class SignatureUtils {

    public static String generateSignature(String timeStamp, String requestContent) {
        String digest = generateDigest(timeStamp, requestContent);
        return join(timeStamp, digest);
    }

    private static String join(String timeStamp, String digest) {
        return timeStamp +
                "," +
                digest;
    }

    private static String generateDigest(String timeStamp, String requestContent) {
        String message = timeStamp + requestContent;
        return HMACSHA256.sha256_HMAC(message);
    }
}
