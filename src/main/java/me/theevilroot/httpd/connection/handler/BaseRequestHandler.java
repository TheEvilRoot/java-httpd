package me.theevilroot.httpd.connection.handler;

import me.theevilroot.httpd.request.HttpRequest;
import me.theevilroot.httpd.response.HttpResponse;

public class BaseRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HttpRequest request) {
        return true;
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws Throwable {
        switch (request.getMethod()) {
            case "GET": return doGet(request);
            case "POST": return doPost(request);
            case "PUT": return doPut(request);
            case "PATCH": return doPatch(request);
            case "HEAD": return doHead(request);
            case "DELETE": return doDelete(request);
            default: return doMethod(request.getMethod(), request);
        }
    }

    public HttpResponse doGet(HttpRequest request) throws Throwable {
        return stubResponse(request);
    }

    public HttpResponse doPost(HttpRequest request) throws Throwable {
        return stubResponse(request);
    }

    public HttpResponse doPut(HttpRequest request) throws Throwable {
        return stubResponse(request);
    }

    public HttpResponse doDelete(HttpRequest request) throws Throwable {
        return stubResponse(request);
    }

    public HttpResponse doPatch(HttpRequest request) throws Throwable {
        return stubResponse(request);
    }

    public HttpResponse doHead(HttpRequest request) throws Throwable {
        return stubResponse(request);
    }

    public HttpResponse doMethod(String method, HttpRequest request) {
        return stubResponse(request);
    }

    private HttpResponse stubResponse(HttpRequest request) {
        return HttpResponse.make(request, 405);
    }

}
