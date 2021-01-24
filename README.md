# Java HTTP server

Simple library that implements http server. 

## Usage

### Running

```java

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();
        HttpServer server = new HttpServer(8888, service);

        /*
          add your handlers to server
         */
        server.addHandler(...);
        
        /*
          initialize server, bind socket to port
         */
        if (!server.init()) {
            System.exit(1);
        }

        /*
          start listen for connections in parallel thread
         */
        if (!server.listen()) {
            service.terminate(5, TimeUnit.SECONDS);
            System.exit(1);
        }

        try {
            service.awaitTermination(1L, TimeUnit.HOURS);
        } catch (InterruptedException ignored) { }
    }
}

```

### Using handlers

`RequestHandler` is a class that can handle some requests and produce response.
To create a handler, you need to implement `RequestHandler` interface and add an instance of your class to `HttpServer` via `addHandler` function.

```java
public class EchoRequestHandler implements RequestHandler {
    
    @Override
    boolean canHandle(final HttpRequest request) {
        // handle only GET requests
        return request.getMethod().equals("GET");
    }
    
    @Override
    HttpResponse handle(final HttpRequest request) {
        HttpResponse response = HttpResponse.make(request, 200);
        response.addHeader("X-Request-Path", request.getPath().toString());
        return response;
    }
    
}
```

This handler will handle all GET requests and send response with header `X-Request-Path` that contains the request path.

### Using BodyWriters

To send body within a response, you need to use `BodyWriter`.

Body writer is an interface that have only one function -- `writeTo(OutputStream)` that should write whatever writer should write to the given stream.

There's a basic implementation for simple data writer -- `DataBodyWriter`. Using this class you can write a byte array or a string to response.

```java

public class EchoRequestHandler implements RequestHandler {
    
    @Override
    boolean canHandle(final HttpRequest request) {
        // handle only GET requests
        return request.getMethod().equals("GET");
    }
    
    @Override
    HttpResponse handle(final HttpRequest request) {
        HttpResponse response = HttpResponse.make(request, 200);
        response.setBodyWriter(new DataBodyWriter(request.getPath().toString()));
        return response;
    }
    
}

```

This handler will send response with body that contains path of the request.

You can add a lot of handlers with different filters (ex. by path components or methods) to route requests.

### UriPath

`UriPath` is utility class that can help work with paths. You can get `UriPath` from the request by calling `getPath()`

For example, if you need a handler that handle only GET requests within `/account/*` route (`/account/get`, `/account/add` etc.)
you can use `UriPath.first()` to get first component of the path

```java
@Override
boolean canHandle(final HttpRequest request) {
    return request.getMethod().equals("GET") && 
        request.getPath().first().equals("account");
}
```

