package me.theevilroot.httpd.connection.handler;

import me.theevilroot.httpd.request.HttpRequest;
import me.theevilroot.httpd.response.HttpResponse;

public interface RequestHandler {

    boolean canHandle(final HttpRequest request);

    HttpResponse handle(final HttpRequest request) throws Throwable;

}
