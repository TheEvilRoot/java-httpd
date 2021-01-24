package me.theevilroot.httpd.connection;

import me.theevilroot.httpd.connection.handler.HandlerProvider;
import me.theevilroot.httpd.connection.handler.RequestHandler;
import me.theevilroot.httpd.request.HttpRequestBuilder;
import me.theevilroot.httpd.response.body.BodyWriter;
import me.theevilroot.httpd.response.HttpResponse;
import me.theevilroot.httpd.util.Log;
import me.theevilroot.httpd.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class HttpConnection implements Runnable {

    private static Long globalId = 0L;

    private final Long connectionId;
    private final Socket socket;

    private final Log logger;

    private final ConnectionHolder holder;
    private final HandlerProvider provider;

    private static final String lineSeparator = String.valueOf(new char[]{ 0x0D, 0x0A });

    public HttpConnection(Socket socket, ConnectionHolder holder, HandlerProvider provider) {
        this.socket = socket;
        this.holder = holder;
        this.provider = provider;
        this.logger = new Log(socket.getInetAddress().getHostAddress());
        this.connectionId = globalId++;
    }

    public Long getConnectionId() {
        return connectionId;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionId);
    }

    @Override
    public void run() {
        try(InputStream stream = socket.getInputStream()) {
            logger.log("InputStream is opened");

            final String method = StreamUtil.readUntil(stream, " ");
            final String path = StreamUtil.readUntil(stream, " ");
            final String protocol = StreamUtil.readUntil(stream, lineSeparator);
            logger.logf("Got %s request with protocol %s", method, protocol);

            HttpRequestBuilder builder = new HttpRequestBuilder(method, path, protocol);

            String header;
            do {
                header = StreamUtil.readUntil(stream, lineSeparator);
                if (header.length() > 2 && header.indexOf('=') > 0) {
                    String[] keyValue = header.split("=", 2);
                    builder.addHeader(keyValue[0], keyValue[1]);
                }
            } while (header.length() > 2);
            logger.log("Headers read complete");

            int contentLength = builder.getContentLength();
            logger.logf("Content length = %d", contentLength);
            if (contentLength > 0 && builder.canHaveBody()) {
                logger.log("Reading body...");
                byte[] body = StreamUtil.bufferedRead(stream, contentLength, 256);
                builder.setBody(body);
                logger.logf("Body read complete. Length = %d", builder.getBodyLength());
            }

            RequestHandler handler = provider.getSuitableHandler(builder);
            HttpResponse response = null;
            if (handler == null) {
                logger.log("No suitable handler found for request");
            } else {
                logger.log("Handling request with RequestHandler");
                response = handler.handle(builder);
                logger.log("Got response from handler");
            }

            if (response == null) {
                logger.log("Get null response");
                response = HttpResponse.make(builder, 501);
            }

            logger.log("Opening output stream...");
            OutputStream outputStream = socket.getOutputStream();

            logger.log("Writing response...");
            writeResponse(response, outputStream);
            logger.log("Response write complete");
        } catch (IOException e) {
            logger.error(e, "Error communicating with client");
        } catch (Throwable thr) {
            logger.error(thr, "Error handling request");
        } finally {
            closeConnection();
        }
    }

    private void writeResponse(HttpResponse response, OutputStream stream) throws IOException {
        String header = response.getHeaderString();
        byte[] lineSeparator = new byte[]{ 0x0D, 0x0A };

        stream.write(header.getBytes(StandardCharsets.UTF_8), 0, header.length());
        stream.write(lineSeparator);

        Iterator<Map.Entry<String, String>> iterator = response.getHeadersIterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            header = next.getKey() + "=" + next.getValue();

            stream.write(header.getBytes(StandardCharsets.UTF_8), 0, header.length());
            stream.write(lineSeparator);
        }
        stream.write(lineSeparator);

        BodyWriter bodyWriter = response.getBodyWriter();
        if (bodyWriter != null) {
            logger.log("Got body writer from response");
            bodyWriter.writeTo(stream);
            logger.log("Body write complete");
        } else {
            logger.log("Response have no body writer. Completing response...");
        }
    }

    private void closeConnection() {
        try {
            if (!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (holder != null)
                holder.notifyDead(this);
        }
    }
}
