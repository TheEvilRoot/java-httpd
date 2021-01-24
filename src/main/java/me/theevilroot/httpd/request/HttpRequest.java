package me.theevilroot.httpd.request;

import me.theevilroot.httpd.util.UriPath;

public interface HttpRequest {

    boolean hasHeader(String key);

    String getHeader(String key);

    String getHeader(String key, String def);

    String getMethod();

    String getProtocol();

    UriPath getPath();

    String getEncodedPath();

    boolean hasBody();

    int getBodyLength();

    byte[] getBody();

}
