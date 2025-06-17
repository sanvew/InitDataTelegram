package io.github.sanvew.tg.init.data.exception;

public class JsonPropertyMissingException extends PropertyMissingException {
    public JsonPropertyMissingException(Class<?> clazz, String property) {
        super("Required property \"" + property +"\" is not provided or null! Class: " + clazz, false);
    }
}
