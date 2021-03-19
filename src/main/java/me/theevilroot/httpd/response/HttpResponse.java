package me.theevilroot.httpd.response;

import me.theevilroot.httpd.request.HttpRequest;
import me.theevilroot.httpd.response.body.BodyWriter;
import me.theevilroot.httpd.util.CodeUtil;

import java.text.SimpleDateFormat;
import java.util.*;

public class HttpResponse {

    public static final SimpleDateFormat responseDateFormat = new SimpleDateFormat("E, dd MMMM yyyy HH:mm:ss Z");

    private final String protocol;
    private final int code;
    private final String message;

    private final String headerString;
    private final String dateString;

    private BodyWriter bodyWriter = null;
    private final HashMap<String, String> headers = new HashMap<>();

    public HttpResponse(String protocol, int code, String message) {
        this.protocol = protocol;
        this.code = code;
        this.message = message;

        this.dateString = responseDateFormat.format(new Date());

        StringBuilder builder = new StringBuilder();
        builder.append(protocol);
        builder.append(' ');
        builder.append(code);
        builder.append(' ');
        builder.append(message);
        this.headerString = builder.toString();

        initDefaultHeaders();
    }

    private void initDefaultHeaders() {
        addHeader("Date", dateString);
        addHeader("Server", "httpd/0.1");
    }

    public String getHeaderString() {
        return headerString;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void removeHeader(String key) {
        headers.remove(key);
    }

    public void setBodyWriter(BodyWriter writer) {
        this.bodyWriter = writer;
        if (bodyWriter != null) {
            addHeader("Content-Length", String.valueOf(bodyWriter.getContentLength()));
        } else {
            removeHeader("Content-Length");
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public String getMessage() {
        return message;
    }

    public String getDateString() {
        return dateString;
    }

    public int getCode() {
        return code;
    }

    public BodyWriter getBodyWriter() {
        return bodyWriter;
    }

    public Iterator<Map.Entry<String, String>> getHeadersIterator() {
        return headers.entrySet().iterator();
    }

    public static HttpResponse make(HttpRequest request, int code, String message) {
        return new HttpResponse(request.getProtocol(), code, message);
    }

    public static HttpResponse make(HttpRequest request, int code) throws NoSuchElementException {
        String message = CodeUtil.getMessageForCode(code);
        if (message == null) {
            throw new NoSuchElementException("Unknown status code " + code);
        }
        return make(request, code, message);
    }

}
