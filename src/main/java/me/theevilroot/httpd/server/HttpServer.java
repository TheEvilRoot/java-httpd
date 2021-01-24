package me.theevilroot.httpd.server;

import me.theevilroot.httpd.connection.ConnectionHolder;
import me.theevilroot.httpd.connection.HttpConnection;
import me.theevilroot.httpd.connection.handler.RequestHandler;
import me.theevilroot.httpd.connection.handler.HandlerProvider;
import me.theevilroot.httpd.request.HttpRequest;
import me.theevilroot.httpd.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class HttpServer implements ConnectionHolder, HandlerProvider {

    private final int port;

    private ServerSocket socket;
    private Log logger = new Log("httpd");

    private final ConcurrentHashMap<Long, HttpConnection> connections = new ConcurrentHashMap<>();
    private final ReentrantLock connectionsLock = new ReentrantLock();

    private final ArrayList<RequestHandler> handlers = new ArrayList<>();
    private final ReentrantLock handlersLock = new ReentrantLock();

    private final ExecutorService service;

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public void addHandler(RequestHandler handler) {
        handlersLock.lock();
        handlers.add(handler);
        handlersLock.unlock();
    }

    public HttpServer(int port, ExecutorService service) {
        this.port = port;
        this.service = service;
        assert port > 0 && port < 0xffff;
    }

    public boolean init() {
        logger.log("Server initialization...");
        if (socket != null) {
            logger.log("Got non-null socket");
            if (socket.isClosed()) {
                socket = null;
            } else {
                logger.log("Server is alive");
                return true;
            }
        }
        try {
            logger.logf("Binding server socket to port %d", port);
            socket = new ServerSocket(port);
        } catch (IOException e) {
            logger.error(e, "new ServerSocket(port)");
            return false;
        }
        return true;
    }

    public boolean listen() {
        if (socket == null) {
            logger.log("listen() with null socket");
            return false;
        }
        service.execute(this::listenRunnable);
        return true;
    }

    public void terminate(long timeout, TimeUnit timeUnit) {
        try {
            if (!service.awaitTermination(timeout, timeUnit)) {
                connectionsLock.lock();
                logger.logf("%d clients not terminated after %d mills. Manual termination...",
                        connections.size(), timeUnit.toMillis(timeout));
                for (HttpConnection connection : connections.values()) {
                    try {
                        connection.getSocket().close();
                    } catch (IOException e) {
                        logger.errorf(e, "close() for %d connection", connection.getConnectionId());
                    }
                }
                connectionsLock.unlock();
                logger.log("Manual connection close is complete. Trying to call awaitTermination()");
                if (!service.awaitTermination(timeout, timeUnit)) {
                    logger.log("awaitTermination() is failed with timeout after manual close.");
                }
            }
            socket.close();
        } catch (InterruptedException e) {
            logger.logf("Error :: awaitTermination has been interrupted");
        } catch (IOException e) {
            logger.error(e, "socket.close() in terminate()");
        }
    }

    private void listenRunnable() {
        logger.logf("Listen in thread %s", Thread.currentThread().getName());
        while (socket != null && !socket.isClosed()) {
            try {
                Socket clientSocket = socket.accept();
                logger.log("accept()",
                        clientSocket.getInetAddress().getHostAddress(), String.valueOf(clientSocket.getPort()));
                addConnection(clientSocket);
            } catch (IOException e) {
                logger.error(e, "socket.accept()");
            }
        }
    }

    private void addConnection(Socket clientSocket) {
        connectionsLock.lock();
        HttpConnection connection = new HttpConnection(clientSocket, this, this);
        connections.put(connection.getConnectionId(), connection);
        logger.logf("Added connection %d", connection.getConnectionId());
        connectionsLock.unlock();
        service.execute(connection);
    }

    private void removeConnection(HttpConnection connection) {
        connectionsLock.lock();
        connections.remove(connection.getConnectionId());
        logger.logf("Removed connection %d", connection.getConnectionId());
        connectionsLock.unlock();
    }

    @Override
    public void notifyDead(HttpConnection connection) {
        removeConnection(connection);
    }

    @Override
    public RequestHandler getSuitableHandler(final HttpRequest request) {
        handlersLock.lock();
        RequestHandler suitable = null;
        for (RequestHandler handler : handlers) {
            if (handler.canHandle(request)) {
                suitable = handler;
                break;
            }
        }
        handlersLock.unlock();
        return suitable;
    }
}

