package me.theevilroot.httpd.request;

public interface HttpRequest {

    boolean hasHeader(String key);

    String getHeader(String key);

    String getHeader(String key, String def);

    String getMethod();

    String getProtocol();

    String getPath();

    String getEncodedPath();

    boolean hasBody();

    int getBodyLength();

    byte[] getBody();

}
