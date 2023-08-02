package com.mkm75.deckshare.app;

import com.google.gson.JsonParseException;
import com.mkm75.deckshare.core.DeckIO;
import com.mkm75.deckshare.core.DeckSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class CLI {
    private static final String[] HELP_CMD;
    static {
        HELP_CMD = new String[] {
                "-h", "--help", "/h", "-?", "/?"
        };
        Arrays.sort(HELP_CMD);
    }
    public static void main(String[] args) {
        System.exit(main0(new ArrayList<>(Arrays.asList(args))));
    }
    private static int main0(List<String> args) {
        if (args.size() == 0) {
            help();
            return -1;
        }
        if (args.size() == 1 && 0<=Arrays.binarySearch(HELP_CMD, args.get(0))) {
            help();
            return 0;
        }

        boolean resultOnly = false;
        if (args.get(0).equalsIgnoreCase("resultOnly")) {
            resultOnly = true;
            args.remove(0);
        }

        if (args.size() < 2) {
            if (!resultOnly)
                System.err.println("引数が不足しています");
            return 1;
        }
        String action = args.remove(0);
        DeckIO.Slot slot = null;
        {
            String slotRaw = args.remove(0);
            try {
                slot = DeckIO.Slot.valueOf(slotRaw);
            } catch (IllegalArgumentException ignored) {
                if (!resultOnly)
                    System.err.println("スロット "+slotRaw+" は不正です。以下のいずれかである必要があります:\n"+
                            Arrays.stream(DeckIO.Slot.values()).map(DeckIO.Slot::toString).toList());
                return 2;
            }
        }
        //noinspection EnhancedSwitchMigration
        switch (action) {
            case "import":
                if (args.isEmpty()) {
                    if (!resultOnly)
                        System.err.println("デッキコードを指定してください");
                    return 4;
                }
                try {
                    String code = args.remove(0);
                    byte[] data = Base64.getDecoder().decode(code);
                    var deck = DeckSerializer.deserialize(data);
                    DeckIO.set(slot, deck);
                } catch (ClassCastException | IllegalArgumentException e) {
                    if (!resultOnly)
                        System.err.println("デッキコードが不正です");
                    return 5;
                } catch (IOException e) {
                    if (!resultOnly)
                        System.err.println("入出力操作中にエラーが発生しました");
                    return 7;
                }
                if (!resultOnly)
                    System.out.println("操作は正常に完了しました");
                return 0;
            case "export":
                try {
                    var deck = DeckIO.get(slot);
                    byte[] data = DeckSerializer.serialize(deck);
                    String code = Base64.getEncoder().encodeToString(data);
                    if (resultOnly) {
                        System.out.print(code);
                    } else {
                        System.out.println("デッキコード: "+code);
                        System.out.println("操作は正常に完了しました");
                    }
                    return 0;

                } catch (JsonParseException e) {
                    if (!resultOnly)
                        System.err.println("セーブデータが存在しないか破損しています");
                    return 6;
                } catch (IOException e) {
                    if (!resultOnly)
                        System.err.println("入出力操作中にエラーが発生しました");
                    return 7;
                }
            default:
                if (!resultOnly)
                    System.err.println(action+" は不正です。 export, import のいずれかである必要があります");
                return 3;
        }
    }

    private static void help() {
        try {
            InputStream is = CLI.class.getResourceAsStream("help.txt");
            BufferedInputStream bis = new BufferedInputStream(Objects.requireNonNull(is));
            InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            char[] buf = new char[1024];
            while (true) {
                int len = br.read(buf);
                if (len == -1) break;
                System.out.print(new String(buf, 0, len));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
