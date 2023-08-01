package com.mkm75.deckshare.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mkm75.deckshare.core.IOUtil.*;
import static com.mkm75.deckshare.core.DigestUtil.*;

public class DeckSerializer {

    private DeckSerializer() {}

    private static final byte[] HEADER = "PZ6D".getBytes(StandardCharsets.UTF_8);
    public static byte[] serialize(List<String> cards) {
        if (256<cards.size()) throw new UnsupportedOperationException();
        Map<String, AtomicInteger> map = new HashMap<>();
        for (var card : cards) {
            if (!map.containsKey(card))
                map.put(card, new AtomicInteger());
            map.get(card).getAndIncrement();
        }
        List<String> singles = new ArrayList<>();
        Map<String, Integer> multiples = new HashMap<>();
        for (var entry : map.entrySet()) {
            int cnt = entry.getValue().get();
            if (cnt == 1)
                singles.add(entry.getKey());
            else
                multiples.put(entry.getKey(), cnt);
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(HEADER);
            baos.write(cards.size());
            baos.write(singles.size());
            for (String card : singles) {
                byte[] raw = CardSerializer.serialize(card);
                baos.write(raw.length);
                baos.write(raw);
            }
            baos.write(multiples.size());
            for (var entry : multiples.entrySet()) {
                baos.write(entry.getValue());
                byte[] raw = CardSerializer.serialize(entry.getKey());
                baos.write(raw.length);
                baos.write(raw);
            }
            byte[] digest = sha256(baos.toByteArray());
            baos.write(digest, 0, 4);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new InternalError(e);
        }
    }
    public static List<String> deserialize(byte[] bytes) {
        {
            byte[] tmp = Arrays.copyOf(bytes, bytes.length-4);
            byte[] digest = sha256(tmp);
            for (int i=0;i<4;i++)
                if (digest[i] != bytes[bytes.length-4+i])
                    throw new ClassCastException();
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            for (int i=0;i<4;i++)
                if (HEADER[i] != read(bais)) throw new ClassCastException();
            int size = Byte.toUnsignedInt(read(bais));
            List<String> list = new ArrayList<>();
            int single = Byte.toUnsignedInt(read(bais));
            for (int i=0;i<single;i++) {
                int len = Byte.toUnsignedInt(read(bais));
                byte[] tmp = read(bais, new byte[len]);
                list.add(CardSerializer.deserialize(tmp));
            }
            int multiple = Byte.toUnsignedInt(read(bais));
            for (int i=0;i<multiple;i++) {
                int cnt = Byte.toUnsignedInt(read(bais));
                int len = Byte.toUnsignedInt(read(bais));
                byte[] tmp = read(bais, new byte[len]);
                String card = CardSerializer.deserialize(tmp);
                for (int j=0;j<cnt;j++)
                    list.add(card);
            }
            if (size != list.size()) throw new ClassCastException();
            Collections.sort(list);
            return list;
        } catch (EOFException e) {
            throw (RuntimeException) new ClassCastException().initCause(e);
        } catch (IOException e) {
            throw new InternalError(e);
        }
    }

}
