package xyz.sanvew.tg.init.data.exception;

public class AuthDateMissingException extends RuntimeException {
    public AuthDateMissingException() {
        super("auth_date is missing");
    }
}
