package me.theevilroot.httpd.util;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {

    private static boolean endsWith(StringBuilder builder, CharSequence sequence) {
        final int builderLength = builder.length();
        final int sequenceLength = sequence.length();

        for (int i = 0 ; i < sequenceLength; i++) {
            if (builder.charAt(builderLength - i - 1) != sequence.charAt(sequenceLength - i - 1)) {
                return false;
            }
        }
        return true;
    }

    public static String readUntil(InputStream stream, CharSequence sequence) throws IOException {
        StringBuilder builder = new StringBuilder();

        while (true) {
            int value = stream.read();
            if (value < 0)
                break;
            char next = (char) value;
            builder.append(next);

            if (builder.length() >= sequence.length() && endsWith(builder, sequence))
                break;
        }

        return builder.substring(0, builder.length() - sequence.length());
    }

    public static byte[] bufferedRead(InputStream stream, int count, int batch) throws IOException {
         byte[] bytes = new byte[count];
         int pointer = 0;

         while (pointer < bytes.length) {
             int currentBatch = Math.min(batch, bytes.length - pointer - 1);
             int read = stream.read(bytes, pointer, currentBatch);
             pointer += read;
         }

         return bytes;
    }

}
