package me.theevilroot.httpd.connection;

public interface ConnectionHolder {

    void notifyDead(HttpConnection connection);

}
