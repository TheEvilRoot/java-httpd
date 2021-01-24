package me.theevilroot.httpd.response.body;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DataBodyWriter implements BodyWriter {

    private final byte[] data;

    public DataBodyWriter(byte[] data) {
        this.data = data;
    }

    public DataBodyWriter(String string) {
        this(string.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
        stream.write(data);
    }
}
