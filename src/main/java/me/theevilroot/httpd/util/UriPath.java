package me.theevilroot.httpd.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class UriPath implements Iterable<String> {

    private final List<String> components;
    private final String stringValue;

    public UriPath(String path) {
        this(splitPath(path));
    }

    private UriPath(List<String> components) {
        this.components = components;
        this.stringValue = joinPathComponents(components);
    }

    public UriPath appendingPathComponent(String component) {
        return new UriPath(components + component);
    }

    public String first() {
        return components.isEmpty() ? null : components.get(0);
    }

    public String at(int index) {
        return index >= components.size() ? null : components.get(index);
    }

    public int count() {
        return components.size();
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public static String joinPathComponents(List<String> components) {
        return components.stream().reduce("", (a, b) -> a + "/" + b);
    }

    public static List<String> splitPath(String path) {
        return Arrays.stream(path.split("/"))
                .map(UriPath::urlDecode)
                .filter((e) -> !e.isEmpty())
                .collect(Collectors.toList());
    }

    public static String urlDecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return encoded;
        }
    }

    @Override
    public Iterator<String> iterator() {
        return components.iterator();
    }
}
