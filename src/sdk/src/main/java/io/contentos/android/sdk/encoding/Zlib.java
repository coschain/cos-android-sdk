package io.contentos.android.sdk.encoding;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Zlib {

    public static byte[] compress(byte[] plain) {
        Deflater compressor = new Deflater();
        compressor.setInput(plain);
        compressor.finish();

        byte[] buffer = new byte[4096];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while (!compressor.finished()) {
            int n = compressor.deflate(buffer);
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    public static byte[] decompress(byte[] compressed) {
        Inflater decompressor = new Inflater();
        decompressor.setInput(compressed);

        byte[] buffer = new byte[4096];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while (!decompressor.finished()) {
            try {
                int n = decompressor.inflate(buffer);
                output.write(buffer, 0, n);
            } catch (Exception e) {
                return null;
            }
        }
        return output.toByteArray();
    }

    public static byte[] compressString(String s) {
        byte[] r;
        try {
            r = compress(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            r = null;
        }
        return r;
    }

    public static String decompressString(byte[] compressed) {
        String s = null;
        try {
            byte[] b = decompress(compressed);
            if (b != null) {
                s = new String(b, 0, b.length, "UTF-8");
            }
        } catch (Exception e) {
            s = null;
        }
        return s;
    }
}
