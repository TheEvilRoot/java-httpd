package me.theevilroot.httpd.response.body;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBodyWriter implements BodyWriter {

    private final Path path;
    private final int batch;

    private File file = null;

    public FileBodyWriter(String filePath, int batch) {
        this.path = Paths.get(filePath);
        this.batch = batch;
    }

    public FileBodyWriter(Path filePath, int batch) {
        this.path = filePath;
        this.batch = batch;
    }

    public FileBodyWriter(File file, int batch) {
        this.path = Paths.get(file.getAbsolutePath());
        this.file = file;
        this.batch = batch;
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
        if (file == null) {
            file = path.toFile();
        }
        if (!file.exists() || !file.canRead())
            throw new IOException("File " + file.getPath() + " cannot be accessed");

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[batch];

        while (true) {
            int read = fileInputStream.read(buffer);
            if (read <= 0)
                break;
            stream.write(buffer, 0, read);
        }

        fileInputStream.close();
    }

    @Override
    public long getContentLength() {
        if (file == null) {
            file = path.toFile();
        }
        return file.length();
    }
}
