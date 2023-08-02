package com.mkm75.deckshare.core;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * コンピューター上の実際にデッキが保存されているファイルに対する読み取り及び書き込みを行います。<br><br>
 * Windowsでは %userprofile%\Appdata\LocalLow\Aineko Games\Puzzline\Save にjson形式で保存されています。
 */
public class DeckIO {
    public enum Slot {
        CURRENT("CustomDeckList"),
        SLOT1("CustomDeckSave1"),
        SLOT2("CustomDeckSave2"),
        SLOT3("CustomDeckSave3"),
        ;
        final String path;
        Slot(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return name().charAt(0)+name().toLowerCase().substring(1);
        }
    }
    private DeckIO() {}
    // TODO Macに対応させる
    private static final File BASEDIR = new File(System.getProperty("user.home").replace('\\', '/')+"/Appdata/LocalLow/Aineko Games/Puzzline/Save");
    public static List<String> get(Slot slot) throws IOException {
        File file = new File(BASEDIR, slot.path);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        InputStreamReader isr = new InputStreamReader(bis);
        BufferedReader br = new BufferedReader(isr);
        Gson gson = new Gson();
        JsonObject jo = gson.fromJson(br, JsonObject.class);
        br.close();
        return jo.get("cardNames").getAsJsonArray().asList().stream()
                .map(JsonElement::getAsString)
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void set(Slot slot, List<String> cards) throws IOException {
        File file = new File(BASEDIR, slot.path);
        if (file.exists()) file.delete();
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        OutputStreamWriter osw = new OutputStreamWriter(bos);
        BufferedWriter bw = new BufferedWriter(osw);
        JsonWriter jw = new JsonWriter(bw);
        jw.setIndent("    ");
        Gson gson = new Gson();
        JsonObject jo = new JsonObject();
        jo.add("cardNames", cards.stream().collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
        gson.toJson(jo, jw);
        jw.flush();
        jw.close();
    }

}
