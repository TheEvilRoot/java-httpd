package me.theevilroot.httpd.util;

public class Log {

    private final String name;

    public Log(String name) {
        this.name = name;
    }

    private String joinStrings(String ... msg) {
        StringBuilder builder = new StringBuilder();
        for (String m : msg) {
            builder.append(m);
            builder.append(" ");
        }
        return builder.toString();
    }

    public void log(String ... msg) {
        System.out.printf("%s :: %s\n", name, joinStrings(msg));
    }

    public void logf(String format, Object ... args) {
        log(String.format(format, args));
    }

    public void error(Throwable exc, String ... msg) {
        System.out.printf("%s :: Error :: %s", name, joinStrings(msg));
        System.out.printf("%s :: %s : %s", name, exc.getClass().getSimpleName(), exc.getLocalizedMessage());
    }

    public void errorf(Throwable exc, String format, Object ... args) {
        error(exc, String.format(format, args));
    }

}
