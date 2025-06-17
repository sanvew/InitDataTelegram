package xyz.sanvew.tg.init.data.exception;

public class AuthDateInvalidException extends RuntimeException {
    public AuthDateInvalidException() {
        super("auth_date is invalid");
    }
}
