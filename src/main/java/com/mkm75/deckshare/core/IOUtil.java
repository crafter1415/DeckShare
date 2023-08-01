package com.mkm75.deckshare.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

class IOUtil {

    private IOUtil() {}

    static byte read(InputStream is) throws IOException {
        int b = is.read();
        if (b == -1) throw new EOFException();
        return (byte) b;
    }
    static byte[] read(InputStream is, byte[] dst) throws IOException {
        if (is.read(dst) != dst.length) throw new EOFException();
        return dst;
    }

}
