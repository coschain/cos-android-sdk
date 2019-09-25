package io.contentos.android.sdk;

import java.util.Random;

public class Utils {

    static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static byte[] hexToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    static String randomAccountName() {
        String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        StringBuilder b = new StringBuilder("ru");
        for (int i = 0; i < 8; i++) {
            b.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return b.toString();
    }

}
