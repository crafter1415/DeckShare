package com.mkm75.deckshare.core;

import java.security.MessageDigest;

/**
 * MessageDigestに関連する操作を行うための簡易的な関数を記載したクラスです。
 */
class DigestUtil {

    private DigestUtil() {}

    static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

}
