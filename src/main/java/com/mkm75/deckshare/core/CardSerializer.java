package com.mkm75.deckshare.core;

/**
 * カード文字列のシリアライズを行います。<br><br>
 * カード名は基本以下の形式になっています:<br>
 * > {@code [A-Za-z0-9]+_[OX]{3}_[OX]{3}}<br>
 * そのため、バイト列への変換時は<br>
 *  - カードの名称部分をBase64とみなし使われていないビットを削除する<br>
 *  - [OX]{3}_[OX]{3} の部分をビットフラグとして1byteにまとめる<br>
 * ことでデータ量を削減しています。<br>
 */
public class CardSerializer {

    private CardSerializer() {}

    public static byte[] serialize(String card) {
        int index = card.indexOf('_');
        String name = card.substring(0, index);
        char[] chars = card.toCharArray();
        byte flag = (byte)0;
        for (int i=0;i<6;i++)
            if (chars[index+1+i+(3<=i?1:0)]=='O')
                flag|=1<<(5-i);
        byte[] nameRaw = Fake64.encode(name);
        byte[] retVal = new byte[nameRaw.length+1];
        System.arraycopy(nameRaw, 0, retVal, 0, nameRaw.length);
        retVal[retVal.length-1]=flag;
        return retVal;
    }

    public static String deserialize(byte[] bytes) {
        byte flag = bytes[bytes.length-1];
        byte[] copy = new byte[bytes.length-1];
        System.arraycopy(bytes, 0, copy, 0, copy.length);
        String name = Fake64.decode(copy);
        char[] chs = "XXX_XXX".toCharArray();
        for (int i=0;i<6;i++)
            if ((flag&(1<<(5-i)))!=0)
                chs[i+(3<=i?1:0)]='O';
        return name+'_'+new String(chs);
    }

}
