package me.theevilroot.httpd.connection.handler;

import me.theevilroot.httpd.request.HttpRequest;

public interface HandlerProvider {

    RequestHandler getSuitableHandler(final HttpRequest request);

}
