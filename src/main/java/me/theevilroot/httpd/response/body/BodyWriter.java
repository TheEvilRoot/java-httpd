package me.theevilroot.httpd.response.body;

import java.io.IOException;
import java.io.OutputStream;

public interface BodyWriter {

    void writeTo(OutputStream stream) throws IOException;

}
