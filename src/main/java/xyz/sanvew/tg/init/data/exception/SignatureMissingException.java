package xyz.sanvew.tg.init.data.exception;

public class SignatureMissingException extends RuntimeException {
    public SignatureMissingException(boolean isThirdParty) {
        super(isThirdParty ? "signature" : "hash" + "parameter is missing");
    }

    public SignatureMissingException() {
        this(false);
    }
}
