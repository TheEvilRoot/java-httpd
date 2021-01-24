package me.theevilroot.httpd.request;

import me.theevilroot.httpd.util.UriPath;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class HttpRequestBuilder implements HttpRequest {

    private final String method;
    private final String path;
    private final String protocol;

    private final String decodedPath;
    private final UriPath uriPath;

    private byte[] body = null;
    private final HashMap<String, String> headers = new HashMap<>();

    public HttpRequestBuilder(String method, String path, String protocol) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.decodedPath = decodePath();
        this.uriPath = new UriPath(path);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getContentLength() {
        if (headers.containsKey("Content-Length")) {
            String header = headers.get("Content-Length");
            try {
                int value = Integer.parseInt(header);
                return Math.max(value, 0);
            } catch (NumberFormatException ignored) { }
        }
        return 0;
    }

    public boolean canHaveBody() {
        return method.equals("POST") || method.equals("PUT");
    }

    private String decodePath() {
        try {
            return URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return path;
        }
    }

    @Override
    public boolean hasHeader(String key) {
        return headers.containsKey(key);
    }

    @Override
    public String getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public String getHeader(String key, String def) {
        if (hasHeader(key))
            return getHeader(key);
        return def;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public UriPath getPath() {
        return uriPath;
    }

    @Override
    public String getEncodedPath() {
        return path;
    }

    @Override
    public boolean hasBody() {
        return body != null && body.length > 0;
    }

    @Override
    public int getBodyLength() {
        return body == null ? 0 : body.length;
    }

    @Override
    public byte[] getBody() {
        return body;
    }
}
