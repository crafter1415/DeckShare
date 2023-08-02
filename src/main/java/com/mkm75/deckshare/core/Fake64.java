package com.mkm75.deckshare.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * アルファベット及び数字のみで構成される文字列をバイト列に圧縮します。<br><br>
 * アルファベット大文字小文字+数字で62文字になるため、割り当てた6bitが0b111110または0b111111になることはありません。
 * そのため、3文字か4文字か分からない3byteの場合などは4文字目が0b11111?か否かで判別します。
 */
public class Fake64 {
    private Fake64() {}
    private static final char[] CHARS;
    static {
        //noinspection SpellCheckingInspection
        CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        Arrays.sort(CHARS);
    }

    public static byte[] encode(String str) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bit=0;
        int bitlen = 0;
        for (char ch : str.toCharArray()) {
            int index = Arrays.binarySearch(CHARS, ch);
            if (index < 0) throw new IllegalArgumentException();
            // index=6bit
            bit <<= 6;
            bit |= index;
            bitlen += 6;
            if (8 <= bitlen) {
                bitlen -= 8;
                baos.write(bit >> bitlen);
                bit &= ~(0xff << bitlen);
            }
        }
        // 一連の操作により 0, 2, 4, 6 のいずれかbit余る
        // 2bit以上余った場合1byteになるまで1埋め
        if (bitlen==0) return baos.toByteArray();
        bit <<= 8-bitlen;
        bit |= 0xff>>bitlen;
        baos.write(bit);
        return baos.toByteArray();
    }
    public static String decode(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        StringBuilder sb = new StringBuilder();
        int bit=0;
        int bitlen = 0;
        while (true) {
            if (6<=bitlen) {
                bitlen-=6;
                int index = bit>>bitlen;
                if (CHARS.length <= index) break;
                sb.append(CHARS[index]);
                bit&=~(0b111111<<bitlen);
            } else {
                int tmp = bais.read();
                if (tmp == -1) break;
                bit<<=8;
                bit|=tmp;
                bitlen+=8;
            }
        }
        return sb.toString();
    }

}
