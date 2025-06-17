package xyz.sanvew.tg.init.data.exception;

public class ExpiredException extends RuntimeException {
    public ExpiredException() {
        super("init data is expired");
    }
}
