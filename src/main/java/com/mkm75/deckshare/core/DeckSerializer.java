package com.mkm75.deckshare.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.mkm75.deckshare.core.IOUtil.*;
import static com.mkm75.deckshare.core.DigestUtil.*;

/**
 * デッキのシリアライズを行います。<br><br>
 * シリアライズではデッキのカードをデッキ内の同じカードの枚数で分け、
 * デッキに1枚しかないカードはシリアライズされたカードを列挙、
 * デッキに複数枚存在するカードはシリアライズされたカードとその枚数をセットで列挙します。
 * 出来上がったコードは、必要であればgzip圧縮された後、ヘッダーが付与された状態でのSHA256が計算され、うち4byteが末尾に添えられます。
 */
public class DeckSerializer {

    private DeckSerializer() {}

    private static final byte[] HEADER = "PZ6D".getBytes(StandardCharsets.UTF_8);
    private static final byte[] GZIP_HEADER = "PZ6G".getBytes(StandardCharsets.UTF_8);
    public static byte[] serialize(List<String> cards) {
        if (256<cards.size()) throw new UnsupportedOperationException();
        Map<String, AtomicInteger> map = new HashMap<>();
        for (var card : cards) {
            if (!map.containsKey(card))
                map.put(card, new AtomicInteger());
            map.get(card).getAndIncrement();
        }
        List<String> singles = new ArrayList<>();
        Map<String, Integer> multiples = new TreeMap<>();
        for (var entry : map.entrySet()) {
            int cnt = entry.getValue().get();
            if (cnt == 1)
                singles.add(entry.getKey());
            else
                multiples.put(entry.getKey(), cnt);
        }
        Collections.sort(singles);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            {
                ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                GZIPOutputStream gzos = new GZIPOutputStream(tmp);
                baos.writeTo(gzos);
                gzos.close();
                if (tmp.size() < baos.size()) {
                    result.write(GZIP_HEADER);
                    tmp.writeTo(result);
                } else {
                    result.write(HEADER);
                    baos.writeTo(result);
                }
            }
            byte[] digest = sha256(result.toByteArray());
            result.write(digest, 0, 4);
            return result.toByteArray();
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
            byte[] header = read(bais, new byte[4]);
            if (Arrays.equals(header, GZIP_HEADER)) {
                byte[] data_gz = new byte[bytes.length-8];
                read(bais, data_gz);
                ByteArrayInputStream tmp = new ByteArrayInputStream(data_gz);
                GZIPInputStream gzis = new GZIPInputStream(tmp);
                byte[] expanded = gzis.readAllBytes();
                bais = new ByteArrayInputStream(expanded);
            } else if (!Arrays.equals(header, HEADER)) {
                throw new ClassCastException();
            }

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
