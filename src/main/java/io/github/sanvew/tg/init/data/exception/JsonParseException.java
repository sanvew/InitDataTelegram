package io.github.sanvew.tg.init.data.exception;

public class JsonParseException extends RuntimeException {
    public JsonParseException(Class<?> clazz, Exception e) {
        super("Unable to parse: " + clazz, e);
    }
}
